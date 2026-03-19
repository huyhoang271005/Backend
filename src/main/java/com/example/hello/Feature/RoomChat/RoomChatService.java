package com.example.hello.Feature.RoomChat;

import com.example.hello.Feature.Message.dto.MessageListInfo;
import com.example.hello.Feature.RoomChat.Repository.RoomChatRepository;
import com.example.hello.Feature.RoomChat.Repository.UserRoomChatRepository;
import com.example.hello.Feature.RoomChat.dto.CountMessageInfo;
import com.example.hello.Feature.RoomChat.dto.RoomChatDTO;
import com.example.hello.Feature.RoomChat.dto.SenderDTO;
import com.example.hello.Feature.RoomChat.dto.UserRomChatInfo;
import com.example.hello.Entity.RoomChat;
import com.example.hello.Entity.UserRoomChat;
import com.example.hello.Infrastructure.Common.Constant.AppProperties;
import com.example.hello.Infrastructure.Common.dto.ListResponse;
import com.example.hello.Infrastructure.Common.dto.Response;
import com.example.hello.Infrastructure.Common.Constant.StringApplication;
import com.example.hello.Feature.User.Repository.UserRepository;
import com.example.hello.Feature.Message.Repository.MessageRepository;
import com.example.hello.Feature.Message.Repository.StatusRepository;
import com.example.hello.Feature.Message.dto.MessageDTO;
import com.example.hello.Feature.Message.MessageStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoomChatService {
    RoomChatRepository roomChatRepository;
    UserRepository userRepository;
    UserRoomChatRepository userRoomChatRepository;
    MessageRepository messageRepository;
    StatusRepository statusRepository;
    AppProperties appProperties;
    @Qualifier("applicationTaskExecutor")
    AsyncTaskExecutor applicationTaskExecutor;
    @Transactional
    public Response<RoomChatDTO> addRoomChat(UUID userCreatedId, RoomChatDTO roomChatDTO) {
        var userIds = roomChatDTO.getUserIds();
        userIds.add(userCreatedId);
        log.info("Room chat has size {}", roomChatDTO.getUserIds().size());
        var roomChat = roomChatRepository.findRoomChatByUserIdInAndSize(userIds, userIds.size())
                .orElseGet(() -> {
                    var users = userRepository.findAllById(userIds);
                    var roomChatCurrent = RoomChat.builder()
                            .roomName(roomChatDTO.getRoomChatName())
                            .createdBy(userCreatedId)
                            .build();
                    roomChatRepository.save(roomChatCurrent);
                    var userRoomChat = users
                            .stream()
                            .map(user -> UserRoomChat.builder()
                                    .user(user)
                                    .roomChat(roomChatCurrent)
                                    .build())
                            .toList();
                    userRoomChatRepository.saveAll(userRoomChat);
                    return roomChatCurrent;
                });
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                RoomChatDTO.builder()
                        .roomChatId(roomChat.getRoomChatId())
                        .roomChatName(roomChat.getRoomName())
                        .build()
        );
    }

    @Transactional(readOnly = true)
    public Response<ListResponse<RoomChatDTO>> getRoomChats(UUID userId, Pageable pageable) {
        var roomChat = roomChatRepository.findRoomChatByUserId(userId, pageable);
        var roomIds = roomChat
                .stream()
                .map(RoomChat::getRoomChatId)
                .toList();
        var messagesCompletable = CompletableFuture.supplyAsync(() ->
                messageRepository.findByRoomChatIds(roomChat.getContent()
                                .stream()
                                .map(RoomChat::getRoomChatId)
                                .toList())
                        .stream()
                        .collect(Collectors.toMap(MessageListInfo::getRoomChatId, Function.identity())),
                applicationTaskExecutor);
        var countMessageCompletable = CompletableFuture.supplyAsync(() ->
                statusRepository.getCountByRoomChatIds(roomIds, MessageStatus.SEND, userId)
                        .stream()
                        .collect(Collectors.toMap(CountMessageInfo::getRoomChatId, Function.identity())),
                applicationTaskExecutor);
        var userCompletable = CompletableFuture.supplyAsync(() ->
                userRoomChatRepository.getUsersRoomChat(roomIds)
                        .stream()
                        .collect(Collectors.groupingBy(UserRomChatInfo::getRoomChatId)),
                applicationTaskExecutor);
        CompletableFuture.allOf(messagesCompletable, countMessageCompletable, userCompletable).join();
        var messages = messagesCompletable.join();
        var countMessage = countMessageCompletable.join();
        var users = userCompletable.join();
        var roomChatDTO = roomChat.getContent()
                .stream()
                .map(roomChat1 ->  {
                    var roomChatDTOCurrent = RoomChatDTO.builder()
                            .roomChatId(roomChat1.getRoomChatId())
                            .roomChatName(roomChat1.getRoomName())
                            .build();
                    var lastMessage = messages.get(roomChat1.getRoomChatId());
                    if(lastMessage != null) {
                        roomChatDTOCurrent.setLastMessage(lastMessage.getContent());
                        roomChatDTOCurrent.setLastMessageTime(lastMessage.getCreatedAt());
                        roomChatDTOCurrent.setSenderId(lastMessage.getSenderId());
                        roomChatDTOCurrent.setMessageSentCount(0);
                        if(countMessage.get(roomChat1.getRoomChatId()) != null) {
                            roomChatDTOCurrent.setMessageSentCount(countMessage.get(roomChat1.getRoomChatId())
                                    .getMessageCount());
                        }
                    }
                    var user = users.get(roomChat1.getRoomChatId())
                            .stream()
                            .filter(userRomChatInfo -> !userRomChatInfo.getUserId().equals(userId))
                            .toList();
                    if(!roomChat1.getRoomName().equals(RoomChatName.GLOBAL.name())){
                        if(users.get(roomChat1.getRoomChatId()).size() == 2){
                            roomChatDTOCurrent.setImageUrl(user.getFirst().getImageUrl());
                            roomChatDTOCurrent.setRoomChatName(user.getFirst().getUsername());
                        }
                        else if(user.isEmpty()){
                            roomChatDTOCurrent.setImageUrl(users.get(roomChat1.getRoomChatId())
                                    .getFirst().getImageUrl());
                            roomChatDTOCurrent.setRoomChatName(RoomChatName.MY_CLOUD.getName());
                        }
                    }
                    else {
                        roomChatDTOCurrent.setRoomChatName(RoomChatName.GLOBAL.getName());
                        roomChatDTOCurrent.setImageUrl(appProperties.getFrontendUrl() + "/android-chrome-512x512.png");
                    }
                    return roomChatDTOCurrent;
                })
                .toList();
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                new ListResponse<>(roomChat.hasNext(), roomChatDTO)
        );
    }
    @Transactional(readOnly = true)
    public Response<List<SenderDTO>> getUsers(UUID userId, UUID roomChatId) {
        var users = userRoomChatRepository.getUsersRoomChat(List.of(roomChatId))
                .stream()
                .map(userRomChatInfo -> SenderDTO.builder()
                        .userId(userRomChatInfo.getUserId())
                        .imageUrl(userRomChatInfo.getImageUrl())
                        .username(userRomChatInfo.getUsername())
                        .roleName(userRomChatInfo.getRoleName())
                        .build())
                .toList();
        users.forEach(senderDTO -> senderDTO.setIsMe(senderDTO.getUserId().equals(userId)));
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                users
        );
    }

    @Transactional(readOnly = true)
    public Response<ListResponse<MessageDTO>> getMessages(UUID userId, UUID roomChatId, Pageable pageable) {
        var messageStatus = statusRepository.getMessageStatusByUserIdAndRoomChatId(
                userId, roomChatId, pageable
        );
        var messageDTO = messageStatus.getContent()
                .stream()
                .map(messageStatusInfo ->  MessageDTO.builder()
                        .roomId(roomChatId)
                        .content(messageStatusInfo.getMessage().getContent())
                        .senderId(messageStatusInfo.getMessage().getSenderId())
                        .time(messageStatusInfo.getUpdatedAt())
                        .messageStatusId(messageStatusInfo.getMessageStatusId())
                        .build())
                .toList();
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                new ListResponse<>(messageStatus.hasNext(), messageDTO)
        );
    }
}

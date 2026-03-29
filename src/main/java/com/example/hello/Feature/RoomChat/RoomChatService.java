package com.example.hello.Feature.RoomChat;

import com.example.hello.Feature.Message.dto.MessageListInfo;
import com.example.hello.Feature.RoomChat.Repository.RoomChatRepository;
import com.example.hello.Feature.RoomChat.Repository.UserRoomChatRepository;
import com.example.hello.Feature.RoomChat.dto.*;
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
import com.example.hello.Feature.Message.dto.MessageStatus;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Infrastructure.Exception.UnprocessableEntityException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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
        if(roomChatDTO.getUserIds().size() == 1 && roomChatDTO.getUserIds().getFirst().equals(userCreatedId)){
            throw new UnprocessableEntityException(StringApplication.ROOM_CHAT.CANT_CHAT_WITH_ME);
        }
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
                                    .roomChatStatus(RoomChatStatus.NORMAL)
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
                .map(RoomChatInfo::getRoomChatId)
                .toList();
        var messagesCompletable = CompletableFuture.supplyAsync(() ->
                messageRepository.findByRoomChatIds(roomChat.getContent()
                                .stream()
                                .map(RoomChatInfo::getRoomChatId)
                                .toList())
                        .stream()
                        .collect(Collectors.toMap(MessageListInfo::getRoomChatId, Function.identity())),
                applicationTaskExecutor);
        var countMessageCompletable = CompletableFuture.supplyAsync(() ->
                statusRepository.getCountByRoomChatIds(roomIds, MessageStatus.SENT, userId)
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
                            .roomChatName(roomChat1.getRoomChatName())
                            .roomChatStatus(roomChat1.getRoomChatStatus())
                            .build();
                    var lastMessage = messages.get(roomChat1.getRoomChatId());
                    if(lastMessage != null ) {
                        roomChatDTOCurrent.setLastMessage(lastMessage.getContent());
                        roomChatDTOCurrent.setLastMessageTime(lastMessage.getCreatedAt());
                        roomChatDTOCurrent.setSenderId(lastMessage.getSenderId());
                        if(countMessage.get(roomChat1.getRoomChatId()) != null) {
                            roomChatDTOCurrent.setMessageSentCount(countMessage.get(roomChat1.getRoomChatId())
                                    .getMessageCount());
                        }
                        if(roomChat1.getDeletedAt() != null &&
                                (roomChat1.getDeletedAt().isAfter(lastMessage.getCreatedAt()) ||
                                roomChat1.getDeletedAt().equals(lastMessage.getCreatedAt()))) {
                            roomChatDTOCurrent.setRoomChatStatus(RoomChatStatus.DELETED);
                        }
                    }

                    var user = users.get(roomChat1.getRoomChatId())
                            .stream()
                            .filter(userRomChatInfo -> !userRomChatInfo.getUserId().equals(userId))
                            .toList();
                    if(users.get(roomChat1.getRoomChatId()).size() == 2){
                        roomChatDTOCurrent.setImageUrl(user.getFirst().getImageUrl());
                        roomChatDTOCurrent.setRoomChatName(user.getFirst().getFullName());
                        roomChatDTOCurrent.setRoleName(user.getFirst().getRoleName());
                    }
                    else if(user.isEmpty()){
                        roomChatDTOCurrent.setRoomChatName(RoomChatName.USER_NOT_FOUND.getName());
                    }
                    else {
                        roomChatDTOCurrent.setImageUrl(appProperties.getFrontendUrl() + "/android-chrome-512x512.png");
                        roomChatDTOCurrent.setRoomChatName(roomChat1.getRoomChatName());
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

    @Transactional
    public Response<Void> deleteRoomChat(UUID userId, UUID roomChatId){
        var userRoomChat = userRoomChatRepository.findByRoomChat_RoomChatIdAndUser_UserId(roomChatId, userId)
                .orElseThrow(() -> new EntityNotFoundException(StringApplication.FIELD.ROOM + StringApplication.FIELD.NOT_EXIST));
        userRoomChat.setDeletedAt(Instant.now());
        CompletableFuture.runAsync(() ->
                roomChatRepository.findById(roomChatId)
                .ifPresent(roomChat ->
                        messageRepository.findFirstByRoomChat_RoomChatIdOrderByCreatedAtDesc(roomChatId)
                        .ifPresent(message -> {
                            var exitsUserRoomNotDelete = roomChat.getUserRoomChats().stream()
                                    .filter(userRoomChat1 -> userRoomChat1.getDeletedAt() != null &&
                                            userRoomChat1.getDeletedAt().isBefore(message.getCreatedAt()))
                                    .toList()
                                    .isEmpty();
                            if(!exitsUserRoomNotDelete){
                                log.info("Delete room chat {} is orphan", roomChatId);
                                roomChatRepository.delete(roomChat);
                            }
                        })), applicationTaskExecutor);
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }

    @Transactional(readOnly = true)
    public Response<List<SenderDTO>> getUsers(UUID userId, UUID roomChatId) {
        var users = userRoomChatRepository.getUsersRoomChat(List.of(roomChatId))
                .stream()
                .map(userRomChatInfo -> SenderDTO.builder()
                        .userId(userRomChatInfo.getUserId())
                        .imageUrl(userRomChatInfo.getImageUrl())
                        .fullName(userRomChatInfo.getFullName())
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
        var userRoom = userRoomChatRepository.findByRoomChat_RoomChatIdAndUser_UserId(roomChatId, userId)
                .orElseThrow(() -> new EntityNotFoundException(StringApplication.FIELD.ROOM + StringApplication.FIELD.NOT_EXIST));
        var messageStatus = statusRepository.getMessageStatusByUserIdAndRoomChatId(
                userId, roomChatId, userRoom.getDeletedAt(), pageable
        );
        var messageDTO = messageStatus.getContent()
                .stream()
                .map(messageStatusInfo ->  MessageDTO.builder()
                        .roomId(roomChatId)
                        .content(messageStatusInfo.getMessage().getContent())
                        .senderId(messageStatusInfo.getMessage().getSenderId())
                        .time(messageStatusInfo.getUpdatedAt())
                        .messageStatusId(messageStatusInfo.getMessageStatusId())
                        .messageId(messageStatusInfo.getMessageId())
                        .status(messageStatusInfo.getMessageStatus())
                        .build())
                .toList();
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                new ListResponse<>(messageStatus.hasNext(), messageDTO)
        );
    }

    @Transactional
    public Response<Void> deleteMessage(UUID userId, UUID roomChatId, UUID messageId) {
        var status = statusRepository.findByMessageIdAndRoomChatIdAndUserId(messageId, roomChatId, userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        StringApplication.FIELD.MESSAGE +
                                StringApplication.FIELD.NOT_EXIST
                ));
        statusRepository.delete(status);
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }

    @Transactional
    public Response<Void> changeStatusRoomChat(UUID userId, UUID roomChatId, RoomChatStatus roomChatStatus) {
        var userRoomChat = userRoomChatRepository.findByRoomChat_RoomChatIdAndUser_UserId(roomChatId, userId)
                .orElseThrow(() -> new EntityNotFoundException(StringApplication.FIELD.ROOM + StringApplication.FIELD.NOT_EXIST));
        userRoomChat.setRoomChatStatus(roomChatStatus);
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }

    @Transactional
    public Response<Void> unreadMessage(UUID userId, UUID roomChatId){
        var status = statusRepository.findFirstByMessage_RoomChat_RoomChatIdAndUser_UserIdOrderByMessage_CreatedAtDesc(roomChatId, userId)
                .orElseThrow(() -> new EntityNotFoundException(StringApplication.FIELD.MESSAGE + StringApplication.FIELD.NOT_EXIST));
        status.setMessageStatus(MessageStatus.SENT);
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }
}

package com.example.hello.Feature.Message;

import com.example.hello.Entity.Message;
import com.example.hello.Entity.Status;
import com.example.hello.Feature.Message.Repository.MessageRepository;
import com.example.hello.Feature.Message.Repository.StatusRepository;
import com.example.hello.Feature.Message.dto.MessageAction;
import com.example.hello.Feature.Message.dto.MessageDTO;
import com.example.hello.Feature.Message.dto.MessageNotificationDTO;
import com.example.hello.Feature.Message.dto.MessageStatus;
import com.example.hello.Feature.RoomChat.Repository.UserRoomChatRepository;
import com.example.hello.Feature.RoomChat.dto.RoomChatStatus;
import com.example.hello.Infrastructure.Common.Constant.StringApplication;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Feature.RoomChat.Repository.RoomChatRepository;
import com.example.hello.Infrastructure.Exception.UnprocessableEntityException;
import com.example.hello.Infrastructure.External.SseEmitter.SseService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageService {
    StatusRepository statusRepository;
    MessageRepository messageRepository;
    RoomChatRepository roomChatRepository;
    SseService sseService;
    private final UserRoomChatRepository userRoomChatRepository;

    @Transactional
    public MessageDTO saveMessage(UUID senderId, MessageDTO messageDTO) {
        var roomChat = roomChatRepository.findById(messageDTO.getRoomId()).orElseThrow(
                () -> new EntityNotFoundException("Room chat not found with id " + messageDTO.getRoomId())
        );
        roomChat.setUpdatedAt(Instant.now());
        var message = Message.builder()
                .content(messageDTO.getContent())
                .senderId(senderId)
                .roomChat(roomChat)
                .build();
        messageRepository.save(message);
        log.info("Message save success.");
        var users = roomChatRepository.getUsersByRoomChatId(messageDTO.getRoomId());
        var statuses = users
                .stream()
                .map(userProfileInfo -> Status.builder()
                        .user(userProfileInfo.getUser())
                        .message(message)
                        .messageStatus(MessageStatus.SENT)
                        .build())
                .toList();
        statusRepository.saveAll(statuses);
        log.info("Status message save success.");
        CompletableFuture.runAsync(() ->
                userRoomChatRepository.findByRoomChat_RoomChatIdAndUser_UserId(messageDTO.getRoomId(), messageDTO.getSenderId())
                .ifPresent(userRoomChat -> {
                    if(userRoomChat.getRoomChatStatus() != RoomChatStatus.MUTE) {
                        users.stream()
                                .filter(userProfileInfo -> !userProfileInfo.getUser().getUserId().equals(senderId))
                                .forEach(userProfileInfo ->
                                        sendSseMessage(senderId, users.stream()
                                                        .map(userProfileInfo1 -> userProfileInfo1.getUser().getUserId())
                                                        .toList(),
                                                MessageNotificationDTO.builder()
                                                        .message(message.getContent())
                                                        .fullName(users.stream()
                                                                .filter(userProfileInfo1 -> userProfileInfo1.getUser().getUserId().equals(senderId))
                                                                .findAny()
                                                                .orElseThrow().getFullName())
                                                        .build()));
                        log.info("Sent sse message success.");
                    }
                }));
        roomChatRepository.save(roomChat);
        log.info("Update time for room chat success.");
        messageDTO.setMessageId(message.getMessageId());
        messageDTO.setSenderId(senderId);
        messageDTO.setTime(Instant.now());
        messageDTO.setStatus(MessageStatus.SENT);
        return messageDTO;
    }

    private void sendSseMessage(UUID senderId, List<UUID> userIds,
                               MessageNotificationDTO messageNotificationDTO){
        userIds = userIds.stream()
                .filter(uuid -> !uuid.equals(senderId))
                .toList();
        sseService.sendSse("message", messageNotificationDTO, userIds);
    }

    @Transactional
    public MessageDTO readMessage(UUID userId, MessageDTO messageDTO){
        var message = messageRepository.findFirstByRoomChat_RoomChatIdOrderByCreatedAtDesc(messageDTO.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("Message not found with id " + messageDTO.getRoomId()));
        if(!message.getSenderId().equals(userId)){
            messageDTO.setSenderId(userId);
        }
        log.info("Room chat id is {}", messageDTO.getRoomId());
        statusRepository.readMessage(MessageStatus.READ, userId, messageDTO.getRoomId());
        log.info("Read message success");
        messageDTO.setAction(MessageAction.READ);
        return messageDTO;
    }

    @Transactional
    public MessageDTO revokeMessage(UUID userId, MessageDTO messageDTO){
        var message = messageRepository.findById(messageDTO.getMessageId()).orElseThrow(
                () -> new EntityNotFoundException("Message not found with id " + messageDTO.getMessageId())
        );
        if(!message.getSenderId().equals(userId)){
            log.error("user id {} different sender id {}, cant revoke", userId, message.getSenderId());
            throw new UnprocessableEntityException(StringApplication.FIELD.REQUEST +
                    StringApplication.FIELD.INVALID);
        }
        messageRepository.delete(message);
        messageDTO.setAction(MessageAction.REVOKE);

        return messageDTO;
    }
}

package com.example.hello.WebSocket.Message;

import com.example.hello.Entity.Message;
import com.example.hello.Entity.Status;
import com.example.hello.Entity.User;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Repository.MessageRepository;
import com.example.hello.Repository.RoomChatRepository;
import com.example.hello.Repository.StatusRepository;
import com.example.hello.SseEmitter.SseService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageService {
    StatusRepository statusRepository;
    MessageRepository messageRepository;
    RoomChatRepository roomChatRepository;
    SseService sseService;

    @Transactional
    public MessageDTO saveMessage(UUID senderId, MessageDTO messageDTO) {
        var roomChat = roomChatRepository.findById(messageDTO.getRoomId()).orElseThrow(
                () -> new EntityNotFoundException("Room chat not found with id " + messageDTO.getRoomId())
        );
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
                .map(user -> Status.builder()
                        .user(user)
                        .message(message)
                        .messageStatus(MessageStatus.SEND)
                        .build())
                .toList();
        statusRepository.saveAll(statuses);
        log.info("Status message save success.");
        sendSseMessage(senderId, users.stream().map(User::getUserId).toList());
        log.info("Sent sse message success.");
        roomChatRepository.save(roomChat);
        log.info("Update time for room chat success.");
        messageDTO.setSenderId(senderId);
        messageDTO.setTime(Instant.now());
        return messageDTO;
    }

    @Async
    public void sendSseMessage(UUID senderId, List<UUID> userIds){
        userIds = userIds.stream()
                .filter(uuid -> !uuid.equals(senderId))
                .toList();
        sseService.sendSse("message", 1, userIds);
    }

    @Transactional
    public void readMessage(UUID senderId, UUID roomId){
        log.info("Room chat id is {}", roomId);
        statusRepository.readMessage(MessageStatus.READ, senderId, roomId);
        log.info("Read message success");
    }
}

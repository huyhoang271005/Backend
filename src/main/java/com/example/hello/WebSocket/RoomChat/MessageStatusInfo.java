package com.example.hello.WebSocket.RoomChat;

import com.example.hello.Entity.Message;
import com.example.hello.WebSocket.Message.MessageStatus;

import java.time.Instant;
import java.util.UUID;

public interface MessageStatusInfo {
    UUID getMessageStatusId();
    MessageStatus getMessageStatus();
    Message getMessage();
    Instant getUpdatedAt();
}

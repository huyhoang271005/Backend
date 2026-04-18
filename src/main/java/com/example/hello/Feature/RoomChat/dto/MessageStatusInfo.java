package com.example.hello.Feature.RoomChat.dto;

import com.example.hello.Entity.Message;
import com.example.hello.Feature.Message.dto.MessageStatus;
import com.example.hello.Feature.Message.dto.MessageType;

import java.time.Instant;
import java.util.UUID;

public interface MessageStatusInfo {
    UUID getMessageStatusId();
    UUID getMessageId();
    MessageStatus getMessageStatus();
    Message getMessage();
    Instant getUpdatedAt();
    MessageType getMessageType();
}

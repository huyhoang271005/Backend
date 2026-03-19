package com.example.hello.Feature.RoomChat.dto;

import com.example.hello.Entity.Message;
import com.example.hello.Feature.Message.MessageStatus;

import java.time.Instant;
import java.util.UUID;

public interface MessageStatusInfo {
    UUID getMessageStatusId();
    MessageStatus getMessageStatus();
    Message getMessage();
    Instant getUpdatedAt();
}

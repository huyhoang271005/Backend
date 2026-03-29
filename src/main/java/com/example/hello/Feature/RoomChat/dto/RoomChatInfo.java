package com.example.hello.Feature.RoomChat.dto;

import java.time.Instant;
import java.util.UUID;

public interface RoomChatInfo {
    UUID getRoomChatId();
    String getRoomChatName();
    RoomChatStatus getRoomChatStatus();
    Instant getDeletedAt();
}

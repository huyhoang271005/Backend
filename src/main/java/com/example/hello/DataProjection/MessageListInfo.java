package com.example.hello.DataProjection;

import java.time.Instant;
import java.util.UUID;

public interface MessageListInfo {
    UUID getRoomChatId();
    UUID getSenderId();
    String getContent();
    Instant getCreatedAt();
}

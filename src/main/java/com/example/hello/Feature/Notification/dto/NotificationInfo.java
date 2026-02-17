package com.example.hello.Feature.Notification.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public interface NotificationInfo {
    UUID getUserNotificationId();
    String getTitle();
    String getMessage();
    String getLinkUrl();
    Boolean getIsRead();
    Instant getCreatedAt();
}

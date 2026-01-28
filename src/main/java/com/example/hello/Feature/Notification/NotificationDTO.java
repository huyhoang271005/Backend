package com.example.hello.Feature.Notification;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationDTO {
    UUID userNotificationId;
    @NotBlank
    String title;
    @NotBlank
    String message;
    Boolean isRead;
    String linkUrl;
    Instant createdAt;
}

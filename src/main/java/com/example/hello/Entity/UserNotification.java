package com.example.hello.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Table(name = "user_notification", indexes = {
        @Index(name = "idx_user_notification_user_id", columnList = "user_id"),
        @Index(name = "idx_user_notification_notification_id", columnList = "notification_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_notification_id")
    UUID userNotificationId;

    @Column(name = "is_read")
    Boolean isRead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id")
    Notification notification;
}

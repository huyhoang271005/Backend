package com.example.hello.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "notification", indexes = {
        @Index(name = "idx_notification_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "notification_id")
    UUID notificationId;

    @Column(columnDefinition = "NVARCHAR(255)")
    String title;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    String message;

    @Column(name = "link_url", columnDefinition = "VARCHAR(255)")
    String linkUrl;

    @Column(name = "created_at")
    @CreationTimestamp
    Instant createdAt;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "notification", cascade = CascadeType.ALL)
    List<UserNotification> userNotifications;
}

package com.example.hello.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_room_chat", indexes = {
        @Index(name = "idx_user_room_chat_user_id", columnList = "user_id"),
        @Index(name = "idx_user_room_chat_room_id", columnList = "room_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRoomChat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_room_chat_id")
    UUID userRoomChatId;

    @Column(name = "joined_at")
    @CreationTimestamp
    Instant joinedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    RoomChat roomChat;
}

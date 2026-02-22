package com.example.hello.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "message", indexes = {
        @Index(name = "idx_message_sender_id", columnList = "sender_id"),
        @Index(name = "idx_message_room_id", columnList = "room_id"),
        @Index(name = "idx_message_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "message_id")
    UUID messageId;

    @Column(name = "sender_id")
    UUID senderId;

    @Column(name = "[content]", columnDefinition = "NVARCHAR(MAX)")
    String content;

    @Column(name = "created_at")
    @CreationTimestamp
    Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    RoomChat roomChat;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "message", cascade = CascadeType.ALL)
    List<Status> statuses;
}

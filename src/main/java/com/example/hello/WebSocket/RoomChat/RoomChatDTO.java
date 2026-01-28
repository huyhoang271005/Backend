package com.example.hello.WebSocket.RoomChat;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomChatDTO {
    UUID roomChatId;
    String roomChatName;
    String ImageUrl;
    List<UUID> userIds;
    String lastMessage;
    UUID senderId;
    Instant lastMessageTime;
    Integer messageSentCount;
}

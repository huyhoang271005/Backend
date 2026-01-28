package com.example.hello.WebSocket.RoomChat;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SenderDTO {
    UUID userId;
    String username;
    String imageUrl;
    String roleName;
    Boolean isMe;
}

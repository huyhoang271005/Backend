package com.example.hello.Feature.RoomChat.dto;

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

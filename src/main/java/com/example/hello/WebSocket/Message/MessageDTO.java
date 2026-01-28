package com.example.hello.WebSocket.Message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageDTO {
    UUID messageStatusId;
    @NotNull
    UUID roomId;
    @NotNull
    UUID senderId;
    @NotBlank
    String content;
    Instant time;
    MessageStatus status;
}

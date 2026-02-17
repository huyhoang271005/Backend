package com.example.hello.Feature.User.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    UUID userId;
    String fullName;
    String username;
    String imageUrl;
    Instant createdAt;
}

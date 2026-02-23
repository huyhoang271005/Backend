package com.example.hello.Feature.User.dto;

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
public class SessionResponse {
    UUID sessionId;
    Boolean validated;
    Boolean revoked;
    Boolean thisSession;
    String deviceName;
    String deviceType;
    String userAgent;
    Address address;
    Instant lastLogin;
    Instant createdAt;
}

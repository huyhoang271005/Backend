package com.example.hello.Feature.User.DTO;

import com.example.hello.Infrastructure.WebClient.IpResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
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
    IpResponse address;
    LocalDateTime lastLogin;
    LocalDateTime createdAt;
}

package com.example.hello.Feature.Authentication.DTO;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginResponse {
    Boolean verifiedEmail;
    Boolean verifiedDevice;
    String accessToken;
    String refreshToken;
}

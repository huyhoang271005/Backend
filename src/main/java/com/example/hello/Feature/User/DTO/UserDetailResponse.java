package com.example.hello.Feature.User.DTO;

import com.example.hello.Enum.Gender;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDetailResponse {
    UUID UserId;
    String username;
    String fullName;
    Gender gender;
    String imageUrl;
    LocalDate birthday;
    Instant createdAt;
    ExtendUserResponse extendUserResponse;
}

package com.example.hello.Users.User.DTO;

import com.example.hello.Users.User.Enum.Gender;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    LocalDateTime createdAt;
    ExtendUserResponse extendUserResponse;
}

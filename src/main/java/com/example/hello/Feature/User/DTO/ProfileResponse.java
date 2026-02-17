package com.example.hello.Feature.User.dto;

import com.example.hello.Enum.Gender;
import com.example.hello.Enum.UserStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileResponse {
    String username;
    List<EmailResponse> emails;
    String fullName;
    Gender gender;
    UserStatus statusName;
    String roleName;
    String imageUrl;
    LocalDate birthday;
    Instant createdAt;
    Instant updatedAt;
}

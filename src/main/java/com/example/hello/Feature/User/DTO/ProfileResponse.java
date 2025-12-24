package com.example.hello.Feature.User.DTO;

import com.example.hello.Enum.Gender;
import com.example.hello.Enum.UserStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}

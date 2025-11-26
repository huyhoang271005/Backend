package com.example.hello.Users.User.DTO;

import com.example.hello.Middleware.Constant;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Users.User.Enum.Gender;
import com.example.hello.Users.User.Enum.UserStatus;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = Constant.VALIDATION.USERNAME, message = StringApplication.FIELD.USERNAME + StringApplication.FIELD.INVALID)
    String username;
    List<EmailResponse> emails;
    String fullName;
    Gender genderName;
    UserStatus statusName;
    String roleName;
    String imageUrl;
    LocalDate birthday;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}

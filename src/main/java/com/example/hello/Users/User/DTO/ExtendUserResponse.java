package com.example.hello.Users.User.DTO;

import com.example.hello.Users.User.Enum.UserStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExtendUserResponse {
    UUID roleId;
    String RoleName;
    UserStatus userStatus;
    List<EmailResponse> emails;
}

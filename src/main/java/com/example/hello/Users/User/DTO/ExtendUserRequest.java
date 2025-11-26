package com.example.hello.Users.User.DTO;

import com.example.hello.Users.User.Enum.UserStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExtendUserRequest {
    UUID userId;
    UserStatus userStatus;
    UUID roleId;
}

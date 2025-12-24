package com.example.hello.Feature.User.DTO;

import com.example.hello.Enum.UserStatus;
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

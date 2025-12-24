package com.example.hello.Feature.User.DTO;

import com.example.hello.Enum.UserStatus;
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
    UserStatus userStatus;
    List<EmailResponse> emails;
}

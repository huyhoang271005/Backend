package com.example.hello.Feature.User.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HomeResponse {
    String appName;
    String imageUrl;
    String username;
    String roleName;
    Integer readNotifications;
    Integer cartsCount;
    Integer readMessages;
}

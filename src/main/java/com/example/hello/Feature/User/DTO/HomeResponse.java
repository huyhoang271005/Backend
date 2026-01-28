package com.example.hello.Feature.User.DTO;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HomeResponse {
    String imageUrl;
    String username;
    String roleName;
    Integer readNotifications;
    Integer cartsCount;
    Integer readMessages;
}

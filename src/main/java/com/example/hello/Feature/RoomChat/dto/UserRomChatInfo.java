package com.example.hello.Feature.RoomChat.dto;

import java.util.UUID;

public interface UserRomChatInfo {
    UUID getRoomChatId();
    UUID getUserId();
    String getUsername();
    String getImageUrl();
    String getRoleName();
}

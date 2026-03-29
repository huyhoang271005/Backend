package com.example.hello.Feature.RoomChat.dto;

import com.example.hello.Infrastructure.Common.Constant.StringApplication;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum RoomChatName {
    USER_NOT_FOUND(StringApplication.ROOM_CHAT.USER_NOT_FOUND);

    String name;
}

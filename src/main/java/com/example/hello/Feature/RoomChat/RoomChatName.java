package com.example.hello.Feature.RoomChat;

import com.example.hello.Infrastructure.Common.Constant.StringApplication;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum RoomChatName {
    GLOBAL(StringApplication.ROOM_CHAT.GLOBAL),
    MY_CLOUD(StringApplication.ROOM_CHAT.MY_CLOUD);

    String name;
}

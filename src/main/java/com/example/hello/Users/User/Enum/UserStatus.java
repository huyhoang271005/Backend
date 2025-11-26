package com.example.hello.Users.User.Enum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserStatus {
    ACTIVE,
    PENDING,
    LOCKED;

    @JsonCreator
    public static UserStatus fromValue(String value) {
        return UserStatus.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}

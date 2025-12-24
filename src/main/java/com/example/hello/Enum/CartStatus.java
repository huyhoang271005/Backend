package com.example.hello.Enum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CartStatus {
    ACTIVE;

    @JsonCreator
    public static CartStatus fromString(String value) {
        return CartStatus.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}

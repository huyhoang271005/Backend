package com.example.hello.Enum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TokenName {
    REFRESH_TOKEN,
    ACCESS_TOKEN;

    @JsonCreator
    public static TokenName forValue(String value) {
        return valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}

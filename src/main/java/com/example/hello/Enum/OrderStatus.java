package com.example.hello.Enum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderStatus {
    WAITING,
    PENDING,
    CONFIRMED,
    PAYING,
    CANCELED,
    DELIVERING,
    DELIVERED;

    @JsonCreator
    public static OrderStatus forValue(String value) {
        return OrderStatus.valueOf(value.toUpperCase());
    }
    @JsonValue
    public String toValue() {
        return this.name();
    }
}

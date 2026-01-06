package com.example.hello.Enum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderStatus {
    CONFIRMED,
    WAITING,
    PAYING,
    PENDING,
    CANCELED,
    DELIVERING,
    DELIVERED,
    COMPLETED;

    @JsonCreator
    public static OrderStatus forValue(String value) {
        return OrderStatus.valueOf(value.toUpperCase());
    }
    @JsonValue
    public String toValue() {
        return this.name();
    }
}

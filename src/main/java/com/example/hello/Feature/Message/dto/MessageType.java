package com.example.hello.Feature.Message.dto;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum MessageType {
    MESSAGE,
    ORDER,
    PRODUCT;

    @JsonCreator
    public static MessageType fromString(String value) {
        return MessageType.valueOf(value.toUpperCase());
    }
}

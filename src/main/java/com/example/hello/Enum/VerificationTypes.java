package com.example.hello.Enum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum VerificationTypes {
    VERIFY_EMAIL,
    VERIFY_DEVICE,
    VERIFY_CHANGE_PASSWORD;

    @JsonCreator
    public static VerificationTypes fromValue(String value) {
        return VerificationTypes.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}

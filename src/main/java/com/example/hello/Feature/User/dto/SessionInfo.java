package com.example.hello.Feature.User.dto;

import com.example.hello.Entity.Session;

import java.time.Instant;
import java.util.UUID;

/**
 * Projection for {@link Session}
 */
public interface SessionInfo {
    UUID getSessionId();

    Boolean getValidated();

    Boolean getRevoked();

    Instant getLastLogin();

    Instant getCreatedAt();

    String getUserAgent();

    String getDeviceName();

    String getDeviceType();

    String getCity();
    String getRegion();
    String getCountry();
    String getTimezone();
}

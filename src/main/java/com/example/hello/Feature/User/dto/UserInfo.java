package com.example.hello.Feature.User.dto;

import com.example.hello.Entity.User;

import java.time.Instant;
import java.util.UUID;

/**
 * Projection for {@link User}
 */
public interface UserInfo {
    UUID getUserId();
    String getUsername();
    String getFullName();
    String getImageUrl();
    Instant getCreatedAt();
}

package com.example.hello.DataProjection;

import java.time.Instant;
import java.util.UUID;

public interface FeedbackInfo {
    String getUsername();
    String getImageUrl();
    UUID getFeedbackId();
    Integer getRating();
    String getComment();
    Instant getCreatedAt();
}

package com.example.hello.Feature.Feedback.dto;

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

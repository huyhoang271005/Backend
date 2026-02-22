package com.example.hello.Feature.Feedback.dto;

import java.time.Instant;
import java.util.UUID;

public interface FeedbackReplyInfo {
    String getUsername();
    String getImageUrl();
    UUID getFeedbackReplyId();
    UUID getFeedbackId();
    String getMessage();
    Instant getCreatedAt();
}

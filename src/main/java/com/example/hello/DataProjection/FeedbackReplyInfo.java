package com.example.hello.DataProjection;

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

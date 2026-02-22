package com.example.hello.Feature.Feedback.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedbackResponse {
    String username;
    String imageUrl;
    UUID feedbackId;
    Integer rating;
    String comment;
    Instant createdAt;
    FeedbackReplyDTO reply;
}

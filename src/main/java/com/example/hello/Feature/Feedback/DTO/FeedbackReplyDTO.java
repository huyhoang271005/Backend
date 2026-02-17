package com.example.hello.Feature.Feedback.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedbackReplyDTO {
    String username;
    String imageUrl;
    UUID feedbackReplyId;
    String message;
    Instant createdAt;
}

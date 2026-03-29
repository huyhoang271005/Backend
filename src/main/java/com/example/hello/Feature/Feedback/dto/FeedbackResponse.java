package com.example.hello.Feature.Feedback.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
    @NotNull
    @Min(1)
    @Max(5)
    Integer rating;
    String comment;
    Instant createdAt;
    FeedbackReplyDTO reply;
}

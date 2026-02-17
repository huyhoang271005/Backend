package com.example.hello.Feature.Feedback.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedbackRequest {
    @NotNull
    @Min(1)
    @Max(5)
    Integer rating;
    String comment;
    @NotEmpty
    @Size(min = 1)
    List<@NotNull UUID> orderItemIds;
}

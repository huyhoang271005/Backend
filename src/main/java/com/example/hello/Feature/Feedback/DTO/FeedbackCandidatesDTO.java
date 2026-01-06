package com.example.hello.Feature.Feedback.DTO;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedbackCandidatesDTO {
    UUID productId;
    String productName;
    String imageUrl;
    List<UUID> orderItemIds;
    List<Map<String, String>> variants;
}

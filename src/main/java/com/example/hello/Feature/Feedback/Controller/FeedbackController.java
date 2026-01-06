package com.example.hello.Feature.Feedback.Controller;

import com.example.hello.Feature.Feedback.DTO.FeedbackRequest;
import com.example.hello.Feature.Feedback.Service.FeedbackService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("feedbacks")
public class FeedbackController {
    FeedbackService feedbackService;

    @GetMapping("/candidates/{orderId}")
    public ResponseEntity<?> getFeedbackCandidates(@PathVariable UUID orderId) {
        return ResponseEntity.ok(feedbackService.getFeedbackCandidates(orderId));
    }

    @PostMapping
    public ResponseEntity<?> addFeedback(@Valid @RequestBody FeedbackRequest feedbackRequest,
                                         @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(feedbackService.addFeedback(userId, feedbackRequest));
    }

    @GetMapping("{productId}")
    public ResponseEntity<?> getFeedbacks(@PathVariable UUID productId,  Pageable pageable) {
        return ResponseEntity.ok(feedbackService.getFeedbacks(productId, pageable));
    }
}

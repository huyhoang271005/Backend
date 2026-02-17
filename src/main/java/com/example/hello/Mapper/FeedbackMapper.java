package com.example.hello.Mapper;

import com.example.hello.Feature.Feedback.dto.FeedbackInfo;
import com.example.hello.Entity.Feedback;
import com.example.hello.Feature.Feedback.dto.FeedbackRequest;
import com.example.hello.Feature.Feedback.dto.FeedbackResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {
    Feedback toFeedback(FeedbackRequest feedbackRequest);
    FeedbackResponse toFeedbackResponse(FeedbackInfo feedbackInfo);
}

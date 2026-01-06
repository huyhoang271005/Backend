package com.example.hello.Mapper;

import com.example.hello.DataProjection.FeedbackInfo;
import com.example.hello.Entity.Feedback;
import com.example.hello.Feature.Feedback.DTO.FeedbackRequest;
import com.example.hello.Feature.Feedback.DTO.FeedbackResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {
    Feedback toFeedback(FeedbackRequest feedbackRequest);
    FeedbackResponse toFeedbackResponse(FeedbackInfo feedbackInfo);
}

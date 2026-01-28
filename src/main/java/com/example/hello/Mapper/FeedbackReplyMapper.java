package com.example.hello.Mapper;

import com.example.hello.DataProjection.FeedbackReplyInfo;
import com.example.hello.Entity.FeedbackReply;
import com.example.hello.Feature.Feedback.DTO.FeedbackReplyDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FeedbackReplyMapper {
    FeedbackReplyDTO toFeedbackReplyDTO(FeedbackReplyInfo feedbackReplyInfo);
}

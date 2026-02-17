package com.example.hello.Feature.Feedback.Service;

import com.example.hello.Entity.FeedbackReply;
import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Middleware.Response;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Feature.Feedback.Repository.FeedbackReplyRepository;
import com.example.hello.Feature.Feedback.Repository.FeedbackRepository;
import com.example.hello.Feature.User.Repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedbackReplyService {
    FeedbackReplyRepository feedbackReplyRepository;
    FeedbackRepository feedbackRepository;
    UserRepository userRepository;

    @Transactional
    public Response<Void> addFeedbackReply(UUID userId, UUID feedbackId,
                                           String message) {
        var user = userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException(StringApplication.FIELD.USER +
                        StringApplication.FIELD.NOT_EXIST));
        var feedback = feedbackRepository.findById(feedbackId).orElseThrow(() ->
                new EntityNotFoundException(StringApplication.FIELD.FEEDBACK +
                        StringApplication.FIELD.NOT_EXIST));
        var exitsFeedbackReply = feedbackReplyRepository
                .existsByFeedback_FeedbackId(feedbackId);
        if(exitsFeedbackReply) {
            throw new ConflictException(StringApplication.ERROR.FEEDBACK_REPLY);
        }
        var feedbackReply = FeedbackReply.builder()
                .feedback(feedback)
                .user(user)
                .message(message)
                .build();
        feedbackReplyRepository.save(feedbackReply);
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }
}

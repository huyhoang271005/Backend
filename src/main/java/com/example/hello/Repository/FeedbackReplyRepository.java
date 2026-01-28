package com.example.hello.Repository;

import com.example.hello.DataProjection.FeedbackReplyInfo;
import com.example.hello.Entity.FeedbackReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface FeedbackReplyRepository extends JpaRepository<FeedbackReply, UUID> {
    Boolean existsByFeedback_FeedbackId(UUID feedbackId);
    @Query("""
            select u.username as username, u.profile.imageUrl as imageUrl,
                        fr.feedbackReplyId as feedbackReplyId, fr.message as message,
                        f.feedbackId as feedbackId, fr.createdAt as createdAt
            from FeedbackReply fr
            join fr.user u
            join fr.feedback f
            where f.feedbackId in :feedbackIds
            """)
    List<FeedbackReplyInfo> getFeedbackRepliesByFeedbackIds(List<UUID> feedbackIds);
}
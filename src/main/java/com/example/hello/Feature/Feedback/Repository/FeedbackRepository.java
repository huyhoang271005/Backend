package com.example.hello.Feature.Feedback.Repository;

import com.example.hello.Feature.Feedback.dto.FeedbackInfo;
import com.example.hello.Entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {
    @Query("""
            select u.username as username, pf.imageUrl as imageUrl,
                        f.feedbackId as feedbackId, f.rating as rating,
                        f.comment as comment, f.createdAt as createdAt
            from Feedback f
            join f.order o
            join o.user u
            join u.profile pf
            join f.product p
            where p.productId = :productId
            order by f.createdAt desc
            """)
    Page<FeedbackInfo> getFeedbackProductInfo(UUID productId, Pageable pageable);

    @Query("""
            select f.feedbackId as feedbackId, f.comment as comment,
                        f.rating as rating, f.createdAt as createdAt,
                        p.imageUrl as imageUrl, p.productName as username
            from Feedback f
            join f.order o
            join f.product p
            join o.user u
            where o.orderId = :orderId and u.userId = :userId
            """)
    List<FeedbackInfo> getFeedbackOrderInfo(UUID orderId, UUID userId);

    Optional<Feedback> findByFeedbackIdAndOrder_User_UserId(UUID feedbackId, UUID userId);
}

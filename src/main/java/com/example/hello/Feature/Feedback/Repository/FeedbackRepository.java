package com.example.hello.Feature.Feedback.Repository;

import com.example.hello.Feature.Feedback.dto.FeedbackInfo;
import com.example.hello.Entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
    Page<FeedbackInfo> getFeedbackInfo(UUID productId, Pageable pageable);
}

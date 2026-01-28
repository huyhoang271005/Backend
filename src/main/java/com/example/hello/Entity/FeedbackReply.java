package com.example.hello.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "feedback_reply")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedbackReply {
    @Id
    @Column(name = "feedback_reply_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID feedbackReplyId;

    String message;

    @Column(name = "created_at")
    @CreationTimestamp
    Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_id", unique = true)
    Feedback feedback;
}

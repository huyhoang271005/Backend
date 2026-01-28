package com.example.hello.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "feedback")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Feedback {
    @Id
    @Column(name = "feedback_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID feedbackId;

    Integer rating;

    String comment;

    @Column(name = "created_at")
    @CreationTimestamp
    Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    Order order;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "feedback", cascade = CascadeType.MERGE)
    List<FeedbackOrderItem> feedbackOrderItems;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "feedback", cascade = CascadeType.ALL)
    List<FeedbackReply> feedbackReplies;
}

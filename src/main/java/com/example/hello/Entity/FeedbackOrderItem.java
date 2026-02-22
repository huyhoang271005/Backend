package com.example.hello.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Table(name = "feedback_order_item", indexes = {
        @Index(name = "idx_feedback_order_item_order_item_id", columnList = "order_item_id"),
        @Index(name = "idx_feedback_order_item_feedback_id", columnList = "feedback_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedbackOrderItem {
    @Id
    @Column(name = "feedback_order_item_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID feedbackOrderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", unique = true)
    OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_id")
    Feedback feedback;
}

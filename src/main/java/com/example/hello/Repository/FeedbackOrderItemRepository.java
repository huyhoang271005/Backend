package com.example.hello.Repository;

import com.example.hello.DataProjection.FeedbackValidator;
import com.example.hello.Entity.FeedbackOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeedbackOrderItemRepository extends JpaRepository<FeedbackOrderItem, UUID> {

    @Query("""
    select
        count(oi.orderItemId) as totalItems,
        count(distinct oi.order.orderId) as orderCount,
        count (distinct foi.orderItem.orderItemId) as feedbackCount
    from OrderItem oi
    left join oi.feedbackOrderItems foi
    where oi.orderItemId in :orderItemIds
    """)
    Optional<FeedbackValidator> validateOrderItemFeedback(List<UUID> orderItemIds);

}
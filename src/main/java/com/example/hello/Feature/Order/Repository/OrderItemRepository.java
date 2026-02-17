package com.example.hello.Feature.Order.Repository;

import com.example.hello.Feature.Order.dto.OrderItemInfo;
import com.example.hello.Entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    @Query("""
            select p.productId as productId, p.productName as productName, p.imageUrl as imageUrl,
                       oi.orderItemId as orderItemId, oi.variant.variantId as variantId,
                                   o.orderStatus as orderStatus
            from OrderItem oi
            left join oi.feedbackOrderItems foi
            join oi.variant.product p
            join oi.order o
            where foi is null and o.orderId = :orderId
            """)
    List<OrderItemInfo> getOrderItemsFeedback(UUID orderId);

    @Query("""
            select v.variantId as variantId, oi.orderItemId as orderItemId,
                        oi.quantity as quantity
            from OrderItem oi
            join oi.variant v
            where oi.order.orderId in :orderIds
            """)
    List<OrderItemInfo> getOrderItemsVariant(List<UUID> orderIds);

    List<OrderItem> findByVariant_VariantIdIn(List<UUID> variantIds);
}

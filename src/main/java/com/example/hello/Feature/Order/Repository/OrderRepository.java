package com.example.hello.Feature.Order.Repository;

import com.example.hello.Feature.Order.dto.GetOrderAndUserId;
import com.example.hello.Feature.Order.dto.GetUserAndOrderId;
import com.example.hello.Feature.Order.dto.OrderInfo;
import com.example.hello.Entity.Order;
import com.example.hello.Enum.OrderStatus;
import com.example.hello.Feature.Order.dto.OrdersCountInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    @Query("""
            select o as order, oi.price as price, oi.originalPrice as originalPrice,
                        oi.quantity as quantity, v.imageUrl as imageUrl, p.productName as productName,
                        v.variantId as variantId, o.paymentAt as paymentAt, o.updatedAt as updatedAt,
                        o.createdAt as createdAt
            from OrderItem oi
            join oi.order o
            join oi.variant v
            join v.product p
            where o.user.userId = :userId and o.orderId = :orderId
            """)
    List<OrderInfo> getOrderInfo(UUID userId, UUID orderId);

    @Query("""
            select o as order, oi.price as price, oi.originalPrice as originalPrice,
                oi.quantity as quantity, v.imageUrl as imageUrl, p.productName as productName,
                v.variantId as variantId, o.createdAt as createdAt, o.paymentMethod as paymentMethod,
                o.updatedAt as updatedAt
                
            from OrderItem oi
            join oi.order o
            join oi.variant v
            join o.user u
            join v.product p
            where u.userId = :userId and
            (cast(o.orderId as string) = :searchInput or p.productName like %:searchInput% or :searchInput is null)
            """)
    Page<OrderInfo> getOrdersInfo(UUID userId, String searchInput, Pageable pageable);

    @Query("""
            select o as order, oi.price as price, oi.originalPrice as originalPrice,
                oi.quantity as quantity, v.imageUrl as imageUrl, p.productName as productName,
                v.variantId as variantId, u.userId as userId
            from OrderItem oi
            join oi.order o
            join oi.variant v
            join o.user u
            join v.product p
            where (:orderStatus IS NULL OR o.orderStatus = :orderStatus)
            AND (:orderId IS NULL OR o.orderId = :orderId)
            order by o.updatedAt desc
            """)
    Page<OrderInfo> getOrdersAdminInfo(Pageable pageable, OrderStatus orderStatus, UUID orderId);

    Optional<Order> findByOrderIdAndUser_UserId(UUID orderId, UUID userUserId);

    Optional<Order> findByPaymentId(String paymentId);


    @Query("""
            select u as user, o.orderId as orderId
            from Order o
            join o.user u
            where o.createdAt <= :timeAgo and o.orderStatus = :orderStatus
            """)
    List<GetUserAndOrderId> findUserOrderByTimeAgo(Instant timeAgo, OrderStatus orderStatus);

    @Modifying
    @Query("""
            update Order o
            set o.orderStatus = :orderStatus, o.updatedAt = :now
            where (case
                        when :isUpdatedAt = true then o.updatedAt
                        else o.createdAt end) <= :timeAgo and o.orderStatus = :currentOrderStatus
            """)
    void updateOrderStatus(Boolean isUpdatedAt, OrderStatus orderStatus, Instant timeAgo, Instant now, OrderStatus currentOrderStatus);

    @Query("""
            select o.orderId
            from Order o
            where (case
                        when :isUpdatedAt = true then o.updatedAt
                        else o.createdAt end) <= :timeAgo and o.orderStatus = :orderStatus
            """)
    List<UUID> findOrderIdsByTimeAgo(Boolean isUpdatedAt, Instant timeAgo, OrderStatus orderStatus);

    @Query("""
            select oi.order.user.userId as userId, oi.order as order
            from OrderItem oi
            where oi.orderItemId = :orderItemId
            """)
    Optional<GetOrderAndUserId> findOrderAndUserIdByOrderItemId(UUID orderItemId);

    @Query("""
            select o.orderStatus as orderStatus, count(o) as orderCount
            from Order o
            join o.user u
            where u.userId = :userId or :userId is null
            group by o.orderStatus
            """)
    List<OrdersCountInfo> CountOrdersByUserId(UUID userId);
}

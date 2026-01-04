package com.example.hello.Repository;

import com.example.hello.Feature.Authentication.DataProjection.OrderInfo;
import com.example.hello.Entity.Order;
import com.example.hello.Entity.User;
import com.example.hello.Enum.OrderStatus;
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
                        v.variantId as variantId, o.paymentAt as paymentAt, o.updatedAt as updatedAt
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
                v.variantId as variantId, o.updatedAt as updatedAt
            from OrderItem oi
            join oi.order o
            join oi.variant v
            join o.user u
            join v.product p
            where u.userId = :userId
            order by o.createdAt desc
            """)
    Page<OrderInfo> getOrdersInfo(UUID userId, Pageable pageable);

    @Query("""
            select o as order, oi.price as price, oi.originalPrice as originalPrice,
                oi.quantity as quantity, v.imageUrl as imageUrl, p.productName as productName,
                v.variantId as variantId
            from OrderItem oi
            join oi.order o
            join oi.variant v
            join o.user u
            join v.product p
            where (:orderStatus IS NULL OR o.orderStatus = :orderStatus)
            AND (:orderId IS NULL OR o.orderId = :orderId)
            order by o.createdAt desc
            """)
    Page<OrderInfo> getOrdersAdminInfo(Pageable pageable, OrderStatus orderStatus, UUID orderId);

    Optional<Order> findByOrderIdAndUser_UserId(UUID orderId, UUID userUserId);

    Optional<Order> findByPaymentId(String paymentId);


    @Query("""
            select u
            from Order o
            join o.user u
            where o.createdAt <= :timeAgo and o.orderStatus = :orderStatus
            """)
    List<User> findUserOrderByTimeAgo(Instant timeAgo, OrderStatus orderStatus);

    @Modifying
    @Query("""
            update Order o
            set o.orderStatus = :orderStatus, o.updatedAt = :now
            where o.updatedAt <= :timeAgo and o.orderStatus = :currentOrderStatus
            """)
    void updateOrderStatus(OrderStatus orderStatus, Instant timeAgo, Instant now, OrderStatus currentOrderStatus);
}
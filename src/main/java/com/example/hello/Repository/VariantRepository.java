package com.example.hello.Repository;

import com.example.hello.DataProjection.VariantInfo;
import com.example.hello.Entity.OrderItem;
import com.example.hello.Entity.Variant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface VariantRepository extends JpaRepository<Variant, UUID> {
    @Query("""
            select v.sold as sold, p.productId as productId
            from Variant v
            join v.product p
            where p.productId in :productIds
            """)
    List<VariantInfo> findVariantInfoByProductIds( List<UUID> productIds);

    @Query("""
            select distinct v.variantId
            from OrderItem oi
            join oi.variant v
            join oi.order o
            where o.orderId = :orderId
            """)
    List<UUID> findVariantIdsByOrderId(UUID orderId);

    List<Variant> findByOrderItems(List<OrderItem> orderItems);
}
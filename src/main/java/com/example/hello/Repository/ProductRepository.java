package com.example.hello.Repository;

import com.example.hello.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
    @Query("""
            select distinct p.productId
            from Variant v
            join v.product p
            where v.variantId in :variantIds
            """)
    List<UUID> findByVariantIds( List<UUID> variantIds);

    @Query("""
            select v.product
            from OrderItem oi
            join oi.variant v
            where oi.orderItemId = :orderItemId
            """)
    Optional<Product> findByOrderItemId(UUID orderItemId);
}
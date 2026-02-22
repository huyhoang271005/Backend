package com.example.hello.Feature.ProductsManager.Repository;

import com.example.hello.Feature.ProductsManager.dto.VariantInfo;
import com.example.hello.Entity.OrderItem;
import com.example.hello.Entity.Variant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}

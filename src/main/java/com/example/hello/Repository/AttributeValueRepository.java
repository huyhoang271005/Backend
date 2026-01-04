package com.example.hello.Repository;

import com.example.hello.Feature.Authentication.DataProjection.ProductAttributesInfo;
import com.example.hello.Entity.AttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AttributeValueRepository extends JpaRepository<AttributeValue, UUID> {
    @Query("""
            select a.attributeId as attributeId, a.attributeName as attributeName,
                    p.productId as productId, av.attributeValueId as attributeValueId,
                    av.value as attributeValueName
            from AttributeValue av
            join av.attribute a
            join av.product p
            where p.productId in :listProductId
            """)
    List<ProductAttributesInfo> getProductAttributes(List<UUID> listProductId);
}
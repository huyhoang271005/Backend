package com.example.hello.Repository;

import com.example.hello.DataProjection.AttributeValueByVariantId;
import com.example.hello.DataProjection.VariantValueInfo;
import com.example.hello.Entity.VariantValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface VariantValueRepository extends JpaRepository<VariantValue, UUID> {
    @Query("""
            select p.productId as productId, vv.attributeValue.attributeValueId as attributeValueId, v.variantId as variantId
            from VariantValue vv
            join vv.variant v
            join v.product p
            where p.productId in :listProductId
            """)
    List<VariantValueInfo> getVariantValueInfo(List<UUID> listProductId);

    @Query("""
            select v.variantId as variantId, av.value as attributeValueName,
                        a.attributeName as attributeName
            from VariantValue vv
            join vv.attributeValue av
            join av.attribute a
            join vv.variant v
            where v.variantId in :listVariantId
            """)
    List<AttributeValueByVariantId> getAttributeValuesVariantIdIn(List<UUID> listVariantId);
}
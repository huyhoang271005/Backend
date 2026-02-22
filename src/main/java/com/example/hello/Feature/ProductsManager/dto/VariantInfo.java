package com.example.hello.Feature.ProductsManager.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Projection for {@link com.example.hello.Entity.CartItem}
 */
public interface VariantInfo {
    UUID getProductId();

    BigDecimal getPrice();

    BigDecimal getOriginalPrice();

    UUID getVariantId();

    Integer getStock();

    Integer getSold();

    String getImageUrl();

    Boolean getActive();
}

package com.example.hello.DataProjection;

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

    String getImageUrl();

    Boolean getActive();
}
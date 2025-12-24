package com.example.hello.DataProjection;

import java.math.BigDecimal;
import java.util.UUID;

public interface CartItemInfo {
    UUID getProductId();
    UUID getVariantId();
    UUID getCartItemId();
    BigDecimal getOldPrice();
    Integer getQuantity();
}

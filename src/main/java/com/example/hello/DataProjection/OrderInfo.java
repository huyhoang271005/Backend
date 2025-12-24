package com.example.hello.DataProjection;

import com.example.hello.Entity.Order;
import com.example.hello.Enum.OrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

public interface OrderInfo {
    Order getOrder();
    UUID getVariantId();
    String getProductName();
    String getImageUrl();
    BigDecimal getOriginalPrice();
    BigDecimal getPrice();
    Integer getQuantity();
}

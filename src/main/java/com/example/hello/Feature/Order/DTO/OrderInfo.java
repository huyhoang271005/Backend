package com.example.hello.Feature.Order.dto;

import com.example.hello.Entity.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface OrderInfo {
    Order getOrder();
    UUID getVariantId();
    String getProductName();
    String getImageUrl();
    BigDecimal getOriginalPrice();
    BigDecimal getPrice();
    Integer getQuantity();
    Instant getPaymentAt();
    Instant getUpdatedAt();
    Instant getCreatedAt();
}

package com.example.hello.DataProjection;

import com.example.hello.Enum.OrderStatus;

import java.util.UUID;

public interface OrderItemInfo {
    UUID getProductId();
    String getProductName();
    String getImageUrl();
    UUID getOrderItemId();
    UUID getVariantId();
    Integer getQuantity();
    OrderStatus getOrderStatus();
}

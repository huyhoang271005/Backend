package com.example.hello.Feature.Order.dto;

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

package com.example.hello.Feature.Order.dto;

import com.example.hello.Enum.OrderStatus;

public interface OrdersCountInfo {
    OrderStatus getOrderStatus();
    Integer getOrderCount();
}

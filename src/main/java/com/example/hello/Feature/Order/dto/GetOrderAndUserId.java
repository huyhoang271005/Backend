package com.example.hello.Feature.Order.dto;

import com.example.hello.Entity.Order;

import java.util.UUID;

public interface GetOrderAndUserId {
    UUID getUserId();
    Order getOrder();
}

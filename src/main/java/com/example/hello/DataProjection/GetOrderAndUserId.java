package com.example.hello.DataProjection;

import com.example.hello.Entity.Order;

import java.util.UUID;

public interface GetOrderAndUserId {
    UUID getUserId();
    Order getOrder();
}

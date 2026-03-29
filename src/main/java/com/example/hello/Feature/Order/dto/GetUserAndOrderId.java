package com.example.hello.Feature.Order.dto;

import com.example.hello.Entity.User;

import java.util.UUID;

public interface GetUserAndOrderId {
    User getUser();
    UUID getOrderId();
}

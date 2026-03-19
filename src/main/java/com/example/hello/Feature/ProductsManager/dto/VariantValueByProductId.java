package com.example.hello.Feature.ProductsManager.dto;

import com.example.hello.Entity.VariantValue;

import java.util.UUID;

public interface VariantValueByProductId {
    UUID getVariantId();
    VariantValue getVariantValue();
}

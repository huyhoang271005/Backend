package com.example.hello.Feature.ProductsManager.dto;

import java.util.UUID;

public interface ProductAttributesInfo {
    UUID getProductId();
    UUID getAttributeId();
    String getAttributeName();
    UUID getAttributeValueId();
    String getAttributeValueName();
}

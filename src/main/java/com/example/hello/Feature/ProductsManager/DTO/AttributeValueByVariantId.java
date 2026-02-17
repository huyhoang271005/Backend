package com.example.hello.Feature.ProductsManager.dto;

import java.util.UUID;

public interface AttributeValueByVariantId {
    UUID getVariantId();
    String getAttributeName();
    String getAttributeValueName();
}

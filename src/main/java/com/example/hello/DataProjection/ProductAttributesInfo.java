package com.example.hello.DataProjection;

import java.util.UUID;

public interface ProductAttributesInfo {
    UUID getProductId();
    UUID getAttributeId();
    String getAttributeName();
    UUID getAttributeValueId();
    String getAttributeValueName();
}

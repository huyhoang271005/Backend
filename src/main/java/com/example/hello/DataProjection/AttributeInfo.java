package com.example.hello.DataProjection;

import java.util.UUID;

/**
 * Projection for {@link com.example.hello.Entity.Attribute}
 */
public interface AttributeInfo {
    UUID getAttributeId();

    String getAttributeName();
}
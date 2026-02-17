package com.example.hello.Feature.Attribute.dto;

import java.util.UUID;

/**
 * Projection for {@link com.example.hello.Entity.Attribute}
 */
public interface AttributeInfo {
    UUID getAttributeId();

    String getAttributeName();
}

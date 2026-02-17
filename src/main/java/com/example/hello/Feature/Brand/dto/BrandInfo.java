package com.example.hello.Feature.Brand.dto;

import java.util.UUID;

/**
 * Projection for {@link com.example.hello.Entity.Brand}
 */
public interface BrandInfo {
    UUID getBrandId();

    String getBrandName();

    String getDescription();
}

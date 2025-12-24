package com.example.hello.DataProjection;

import java.util.UUID;

/**
 * Projection for {@link com.example.hello.Entity.Brand}
 */
public interface BrandInfo {
    UUID getBrandId();

    String getBrandName();

    String getDescription();
}
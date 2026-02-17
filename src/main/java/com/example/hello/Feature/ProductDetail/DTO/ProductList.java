package com.example.hello.Feature.ProductDetail.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductList {
    UUID productId;
    String productName;
    String imageUrl;
    BigDecimal originalPrice;
    BigDecimal price;
    Integer totalSales;
    Double ratingAvg;
}

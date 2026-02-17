package com.example.hello.Feature.Cart.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductVariantsDTO {
    UUID variantId;
    String imageUrl;
    Integer stock;
    BigDecimal price;
    BigDecimal originalPrice;
    Boolean active;
    List<UUID> attributeValueIdList;
}

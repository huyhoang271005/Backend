package com.example.hello.Feature.Order.DTO;

import jakarta.validation.constraints.NotNull;
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
public class OrderItemDTO {
    UUID cartItemId;
    @NotNull
    UUID variantId;
    List<String> attributeValues;
    String productName;
    BigDecimal originalPrice;
    BigDecimal price;
    String imageUrl;
    Integer quantity;
}

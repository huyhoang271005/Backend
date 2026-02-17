package com.example.hello.Feature.ProductsManager.dto;

import com.example.hello.Middleware.StringApplication;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductDetailDTO {
    UUID productId;
    @NotNull
    String productName;
    String description;
    String imageUrl;
    @NotNull
    @DecimalMin(value = "100", inclusive = false, message = StringApplication.ERROR.MONEY)
    @Digits(integer = 18, fraction = 0, message = StringApplication.ERROR.MONEY)
    BigDecimal originalPrice;
    @NotNull
    @DecimalMin(value = "100", inclusive = false, message = StringApplication.ERROR.MONEY)
    @Digits(integer = 18, fraction = 0, message = StringApplication.ERROR.MONEY)
    BigDecimal price;
    @NotNull
    UUID categoryId;
    @NotNull
    UUID brandId;
    Integer totalSales;
    Double ratingAvg;
    Integer ratingCount;
    Instant createdAt;
    Instant updatedAt;
}

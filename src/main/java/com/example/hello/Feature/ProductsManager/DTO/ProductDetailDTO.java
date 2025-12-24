package com.example.hello.Feature.ProductsManager.DTO;

import com.example.hello.Middleware.StringApplication;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
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
    @DecimalMin(value = "0.0", message = StringApplication.ERROR.MONEY)
    Double originalPrice;
    @DecimalMin(value = "0.0", message = StringApplication.ERROR.MONEY)
    Double price;
    @NotNull
    UUID categoryId;
    @NotNull
    UUID brandId;
    Integer totalSales;
    Double ratingAvg;
    Integer ratingCount;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}

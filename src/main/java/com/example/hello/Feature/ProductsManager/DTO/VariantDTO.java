package com.example.hello.Feature.ProductsManager.DTO;


import com.example.hello.Middleware.StringApplication;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VariantDTO {
    String variantId;
    String imageName;
    @NotNull
    @DecimalMin(value = "100", inclusive = false, message = StringApplication.ERROR.MONEY)
    @Digits(integer = 18, fraction = 0, message = StringApplication.ERROR.MONEY)
    BigDecimal originalPrice;
    @NotNull
    @DecimalMin(value = "100", inclusive = false, message = StringApplication.ERROR.MONEY)
    @Digits(integer = 18, fraction = 0, message = StringApplication.ERROR.MONEY)
    BigDecimal price;
    @NotNull
    @Min(value = 1, message = StringApplication.ERROR.NUMBER)
    Integer stock;
    Integer sold;
    String imageUrl;
    Boolean active;
}

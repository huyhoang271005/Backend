package com.example.hello.Feature.ProductsManager.DTO;


import com.example.hello.Middleware.StringApplication;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VariantDTO {
    String variantId;
    String imageName;
    @DecimalMin(value = "0.0", message = StringApplication.ERROR.MONEY)
    Double originalPrice;
    @DecimalMin(value = "0.0", message = StringApplication.ERROR.MONEY)
    Double price;
    @NotNull
    @Min(value = 1, message = StringApplication.ERROR.NUMBER)
    Integer stock;
    Integer sold;
    String imageUrl;
    Boolean active;
}

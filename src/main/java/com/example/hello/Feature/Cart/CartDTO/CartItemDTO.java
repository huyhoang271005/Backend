package com.example.hello.Feature.Cart.CartDTO;

import com.example.hello.Middleware.StringApplication;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemDTO {
    UUID cartItemId;
    @NotNull
    UUID variantId;
    BigDecimal oldPrice;
    @NotNull
    @Min(value = 1, message = StringApplication.ERROR.NUMBER)
    Integer quantity;
}

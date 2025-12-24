package com.example.hello.Feature.Cart.CartDTO;

import com.example.hello.Feature.ProductsManager.DTO.AttributeDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartDTO {
    String productName;
    List<AttributeDTO> attributes;
    List<ProductVariantsDTO> productVariantsDTOList;
    List<CartItemDTO> cartItemDTOList;
}

package com.example.hello.Feature.Cart.dto;

import com.example.hello.Feature.ProductsManager.dto.AttributeDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartDTO {
    UUID productId;
    String productName;
    List<AttributeDTO> attributes;
    List<ProductVariantsDTO> productVariantsDTOList;
    List<CartItemDTO> cartItemDTOList;
}

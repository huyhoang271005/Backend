package com.example.hello.Feature.ProductsManager.DTO;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductDTO {
    ProductDetailDTO productDetailDTO;
    List<AttributeDTO> attributes;
    List<VariantValueDTO> variantValues;
    List<VariantDTO> variants;
}

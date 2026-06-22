package com.example.hello.Feature.ProductsManager.dto;

import jakarta.validation.Valid;
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
    @Valid
    ProductDetailDTO productDetailDTO;
    List<AttributeDTO> attributes;
    List<VariantValueDTO> variantValues;
    @Valid
    List<VariantDTO> variants;
}

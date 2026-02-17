package com.example.hello.Feature.ProductDetail.dto;

import com.example.hello.Feature.Cart.dto.ProductVariantsDTO;
import com.example.hello.Feature.ProductsManager.dto.AttributeDTO;
import com.example.hello.Feature.ProductsManager.dto.ProductDetailDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductDetailResponse {
    ProductDetailDTO productDetailDTO;
    List<AttributeDTO> attributeDTOList;
    List<ProductVariantsDTO> productVariantsDTO;
}

package com.example.hello.Feature.ProductDetail.DTO;

import com.example.hello.Feature.Cart.CartDTO.ProductVariantsDTO;
import com.example.hello.Feature.ProductsManager.DTO.AttributeDTO;
import com.example.hello.Feature.ProductsManager.DTO.ProductDetailDTO;
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

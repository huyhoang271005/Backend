package com.example.hello.Feature.ProductsManager.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VariantValueDTO {
    String attributeValueId;
    String variantId;
}

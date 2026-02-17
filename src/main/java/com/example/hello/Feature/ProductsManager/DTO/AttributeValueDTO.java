package com.example.hello.Feature.ProductsManager.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttributeValueDTO {
    String attributeValueId;
    @NotNull
    String attributeValueName;
}

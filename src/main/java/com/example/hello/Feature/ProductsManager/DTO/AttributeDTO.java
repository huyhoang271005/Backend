package com.example.hello.Feature.ProductsManager.DTO;

import jakarta.validation.constraints.NotNull;
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
public class AttributeDTO {
    @NotNull
    UUID attributeId;
    String attributeName;
    List<AttributeValueDTO> attributeValues;
}

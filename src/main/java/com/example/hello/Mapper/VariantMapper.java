package com.example.hello.Mapper;

import com.example.hello.Entity.Variant;
import com.example.hello.Feature.ProductsManager.DTO.VariantDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VariantMapper {
    VariantDTO toVariantDTO(Variant variant);
    @Mapping(target = "variantId", ignore = true)
    @Mapping(target = "sold", ignore = true)
    void updateVariant(VariantDTO variantDTO, @MappingTarget Variant variant);
}

package com.example.hello.Mapper;

import com.example.hello.Feature.ProductsManager.dto.VariantInfo;
import com.example.hello.Entity.CartItem;
import com.example.hello.Feature.Cart.dto.CartItemDTO;
import com.example.hello.Feature.Cart.dto.ProductVariantsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemMapper {
    ProductVariantsDTO toProductVariant(VariantInfo variantInfo);

    @Mapping(target = "oldPrice", ignore = true)
    CartItem toCartItem(CartItemDTO cartItemDTO);
}

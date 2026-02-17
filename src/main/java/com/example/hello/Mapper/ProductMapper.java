package com.example.hello.Mapper;

import com.example.hello.Entity.Product;
import com.example.hello.Feature.ProductDetail.dto.ProductList;
import com.example.hello.Feature.ProductsManager.dto.ProductDetailDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {
    ProductDetailDTO toProductDTO(Product product);

    @Mapping(target = "totalSales", ignore = true)
    @Mapping(target = "ratingAvg", ignore = true)
    @Mapping(target = "ratingCount", ignore = true)
    void updateProduct(ProductDetailDTO productDetailDTO,  @MappingTarget Product product);
    List<ProductList> toProductList(List<Product> productList);
}

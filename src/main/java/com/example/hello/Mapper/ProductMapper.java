package com.example.hello.Mapper;

import com.example.hello.Entity.Product;
import com.example.hello.Feature.ProductDetail.DTO.ProductList;
import com.example.hello.Feature.ProductsManager.DTO.ProductDetailDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {
    ProductDetailDTO toProductDTO(Product product);
    void updateProduct(ProductDetailDTO productDetailDTO,  @MappingTarget Product product);
    List<ProductList> toProductList(List<Product> productList);
}

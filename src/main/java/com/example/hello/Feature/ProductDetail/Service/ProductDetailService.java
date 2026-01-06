package com.example.hello.Feature.ProductDetail.Service;

import com.example.hello.DataProjection.ProductAttributesInfo;
import com.example.hello.DataProjection.VariantValueInfo;
import com.example.hello.Entity.Product;
import com.example.hello.Feature.ProductDetail.DTO.ProductDetailResponse;
import com.example.hello.Feature.ProductDetail.DTO.ProductList;
import com.example.hello.Feature.ProductDetail.Specification.ProductSpecification;
import com.example.hello.Feature.ProductsManager.DTO.AttributeDTO;
import com.example.hello.Feature.ProductsManager.DTO.AttributeValueDTO;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Mapper.CartItemMapper;
import com.example.hello.Mapper.ProductMapper;
import com.example.hello.Middleware.ListResponse;
import com.example.hello.Middleware.Response;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductDetailService {
    ProductRepository productRepository;
    AttributeValueRepository attributeValueRepository;
    CartItemRepository cartItemRepository;
    VariantValueRepository variantValueRepository;
    ProductMapper productMapper;
    CartItemMapper cartItemMapper;

    @Transactional(readOnly = true)
    public Response<ProductDetailResponse> getProductDetail(UUID productId) {
        var product = productRepository.findById(productId).orElseThrow(
                ()-> new EntityNotFoundException(StringApplication.FIELD.PRODUCT +
                        StringApplication.FIELD.NOT_EXIST)
        );
        log.info("Found product detail successfully");
        var attributeValues = attributeValueRepository.getProductAttributes(List.of(productId)).stream()
                .collect(Collectors.groupingBy(ProductAttributesInfo::getAttributeId));
        log.info("Found attribute values successfully");
        var variants = cartItemRepository.getProductVariants(List.of(productId));
        log.info("Found variant values successfully");
        var variantValues = variantValueRepository.getVariantValueInfo(List.of(productId))
                .stream()
                .collect(Collectors.groupingBy(VariantValueInfo::getVariantId));
        log.info("Group by variant values successfully");
        var productDetailResponse = ProductDetailResponse.builder()
                .productDetailDTO(productMapper.toProductDTO(product))
                .attributeDTOList(attributeValues.keySet().stream()
                        .map(attributeId -> AttributeDTO.builder()
                                .attributeId(attributeId)
                                .attributeName(attributeValues.get(attributeId)
                                        .getFirst().getAttributeName())
                                .attributeValues(attributeValues.get(attributeId)
                                        .stream()
                                        .map(productAttributesInfo -> AttributeValueDTO.builder()
                                                .attributeValueId(String.valueOf(productAttributesInfo.getAttributeValueId()))
                                                .attributeValueName(productAttributesInfo.getAttributeValueName())
                                                .build())
                                        .toList())
                                .build())
                        .toList())
                .productVariantsDTO(variants.stream()
                        .map(variantInfo -> {
                            var v = cartItemMapper.toProductVariant(variantInfo);
                            v.setAttributeValueIdList(variantValues.get(variantInfo.getVariantId()).stream()
                                    .map(VariantValueInfo::getAttributeValueId)
                                    .toList());
                            return v;
                        })
                        .toList())
                .build();
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                productDetailResponse
        );
    }

    @Transactional(readOnly = true)
    public Response<ListResponse<ProductList>> getProductList(String productName, UUID categoryId,
                                                              UUID brandId,
                                                              BigDecimal minPrice,
                                                              BigDecimal maxPrice,
                                                              Pageable pageable) {
        Specification<Product> specification = ProductSpecification.hasCategory(categoryId)
                .and(ProductSpecification.likeName(productName))
                .and(ProductSpecification.hasBrand(brandId))
                .and(ProductSpecification.betweenPrice(minPrice, maxPrice));
        var productPage = productRepository.findAll(specification, pageable);
        log.info("Found product list successfully");
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                new ListResponse<>(
                        productPage.hasNext(),
                        productMapper.toProductList(productPage.getContent())
                )
        );
    }
}

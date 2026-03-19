package com.example.hello.Feature.ProductDetail.Service;

import com.example.hello.Feature.ProductsManager.dto.ProductAttributesInfo;
import com.example.hello.Feature.ProductsManager.dto.VariantValueInfo;
import com.example.hello.Entity.Product;
import com.example.hello.Feature.ProductDetail.dto.ProductDetailResponse;
import com.example.hello.Feature.ProductDetail.dto.ProductList;
import com.example.hello.Feature.ProductDetail.Specification.ProductSpecification;
import com.example.hello.Feature.ProductsManager.dto.AttributeDTO;
import com.example.hello.Feature.ProductsManager.dto.AttributeValueDTO;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Mapper.CartItemMapper;
import com.example.hello.Mapper.ProductMapper;
import com.example.hello.Infrastructure.Common.dto.ListResponse;
import com.example.hello.Infrastructure.Common.dto.Response;
import com.example.hello.Infrastructure.Common.Constant.StringApplication;
import com.example.hello.Feature.ProductsManager.Repository.ProductRepository;
import com.example.hello.Feature.Attribute.AttributeValueRepository;
import com.example.hello.Feature.Cart.CartItemRepository;
import com.example.hello.Feature.ProductsManager.Repository.VariantValueRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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
    @Qualifier("applicationTaskExecutor")
    AsyncTaskExecutor applicationTaskExecutor;

    @Transactional(readOnly = true)
    public Response<ProductDetailResponse> getProductDetail(UUID productId) {
        var productCompletable = CompletableFuture.supplyAsync(() ->
                        productRepository.findById(productId).orElseThrow(
                                ()-> new EntityNotFoundException(StringApplication.FIELD.PRODUCT +
                                        StringApplication.FIELD.NOT_EXIST)));
        var attributeValuesCompletable = CompletableFuture
                .supplyAsync(() -> attributeValueRepository.getProductAttributes(List.of(productId)).stream()
                        .collect(Collectors.groupingBy(ProductAttributesInfo::getAttributeId)),
                        applicationTaskExecutor);
        var variantsCompletable = CompletableFuture.supplyAsync(() ->
                cartItemRepository.getProductVariants(List.of(productId)), applicationTaskExecutor);
        var variantValuesCompletable = CompletableFuture.supplyAsync(() ->
                        variantValueRepository.getVariantValueInfo(List.of(productId))
                                .stream()
                                .collect(Collectors.groupingBy(VariantValueInfo::getVariantId)),
                applicationTaskExecutor);
        CompletableFuture.allOf(productCompletable, attributeValuesCompletable, variantsCompletable, variantValuesCompletable)
                .join();
        var product = productCompletable.join();
        log.info("Found product detail successfully");
        var attributeValues = attributeValuesCompletable.join();
        log.info("Found attribute values successfully");
        var variants = variantsCompletable.join();
        log.info("Found variant values successfully");
        var variantValues = variantValuesCompletable.join();
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
        var productPage = productRepository.findAll(specification,
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt")));
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

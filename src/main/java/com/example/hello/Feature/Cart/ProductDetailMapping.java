package com.example.hello.Feature.Cart;

import com.example.hello.Feature.ProductsManager.dto.VariantValueInfo;
import com.example.hello.Feature.ProductsManager.dto.ProductInfo;
import com.example.hello.Feature.ProductsManager.dto.VariantInfo;
import com.example.hello.Feature.Cart.dto.CartItemInfo;
import com.example.hello.Feature.ProductsManager.dto.ProductAttributesInfo;
import com.example.hello.Feature.Cart.dto.CartDTO;
import com.example.hello.Feature.Cart.dto.CartItemDTO;
import com.example.hello.Feature.ProductsManager.dto.AttributeDTO;
import com.example.hello.Feature.ProductsManager.dto.AttributeValueDTO;
import com.example.hello.Mapper.CartItemMapper;
import com.example.hello.Feature.ProductsManager.Repository.VariantValueRepository;
import com.example.hello.Feature.Attribute.AttributeValueRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,  makeFinal = true)
public class ProductDetailMapping {
    CartItemRepository cartItemRepository;
    VariantValueRepository variantValueRepository;
    CartItemMapper cartItemMapper;
    AttributeValueRepository attributeValueRepository;
    @Qualifier("applicationTaskExecutor")
    AsyncTaskExecutor applicationTaskExecutor;

    @Transactional(readOnly = true)
    public List<CartDTO> mappingProductsDetail(Map<UUID,List<ProductInfo>> products, UUID userId) {
        var productIds = products.keySet()
                .stream()
                .toList();
        var attributeValuesCompletable = CompletableFuture.supplyAsync(() ->
                attributeValueRepository.getProductAttributes(productIds), applicationTaskExecutor);
        var productVariantsCompletable = CompletableFuture.supplyAsync(() ->
                cartItemRepository.getProductVariants(productIds)
                        .stream()
                        .collect(Collectors.groupingBy(VariantInfo::getProductId)),
                applicationTaskExecutor);
        var productVariantValuesCompletable = CompletableFuture.supplyAsync(() ->
                        variantValueRepository.getVariantValueInfo(productIds)
                                .stream()
                                .collect(Collectors.groupingBy(VariantValueInfo::getProductId)),
                applicationTaskExecutor);
        var productCartItemsCompletable = CompletableFuture.supplyAsync(() ->
                cartItemRepository.getCartItemByProductId(productIds, userId).stream()
                        .collect(Collectors.groupingBy(CartItemInfo::getProductId)),
                applicationTaskExecutor);
        CompletableFuture.allOf(attributeValuesCompletable, productVariantsCompletable,
                productVariantValuesCompletable, productCartItemsCompletable).join();
        log.info("Get product id successfully");
        var attributeValuesCurrent = attributeValuesCompletable.join();
        log.info("Found attribute values successfully");
        var productAttributeValues = attributeValuesCurrent.stream()
                .collect(Collectors.groupingBy(ProductAttributesInfo::getProductId));
        log.info("Group by attribute values successfully");
        var productVariants = productVariantsCompletable.join();
        log.info("Found and group by variant successfully");
        var productVariantValues = productVariantValuesCompletable.join();
        log.info("Found and group by variant values successfully");
        var productCartItem = productCartItemsCompletable.join();
        log.info("Found and group by cart item successfully");
        return productIds.stream()
                .map(productId -> {
                    var variants = productVariants.get(productId);
                    var attributeValues = productAttributeValues.get(productId)
                            .stream()
                            .collect(Collectors.groupingBy(ProductAttributesInfo::getAttributeId));
                    var variantValues = productVariantValues.get(productId)
                            .stream()
                            .collect(Collectors.groupingBy(VariantValueInfo::getVariantId));
                    var cartItems = productCartItem.get(productId);
                    return CartDTO.builder()
                            .productId(productId)
                            .productName(products.get(productId).getFirst().getProductName())
                            .cartItemDTOList(cartItems.stream()
                                    .map(cartItemInfo -> CartItemDTO.builder()
                                            .cartItemId(cartItemInfo.getCartItemId())
                                            .variantId(cartItemInfo.getVariantId())
                                            .oldPrice(cartItemInfo.getOldPrice())
                                            .quantity(cartItemInfo.getQuantity())
                                            .build())
                                    .toList())
                            .attributes(attributeValues.keySet().stream()
                                    .map(attributeId -> AttributeDTO.builder()
                                            .attributeId(attributeId)
                                            .attributeName(attributeValues.get(attributeId).getFirst().getAttributeName())
                                            .attributeValues(attributeValues.get(attributeId).stream()
                                                    .map(productAttributesInfo -> AttributeValueDTO.builder()
                                                            .attributeValueId(String.valueOf(productAttributesInfo.getAttributeValueId()))
                                                            .attributeValueName(productAttributesInfo.getAttributeValueName())
                                                            .build())
                                                    .toList())
                                            .build())
                                    .toList())
                            .productVariantsDTOList(variants.stream()
                                    .map(variantInfo -> {
                                        var cartItemDTO = cartItemMapper.toProductVariant(variantInfo);
                                        cartItemDTO.setAttributeValueIdList(variantValues.get(variantInfo.getVariantId())
                                                .stream()
                                                .map(VariantValueInfo::getAttributeValueId)
                                                .toList());
                                        return  cartItemDTO;
                                    })
                                    .toList())
                            .build();
                })
                .toList();
    }
}

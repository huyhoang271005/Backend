package com.example.hello.Feature.ProductsManager.Service;

import com.example.hello.Entity.Product;
import com.example.hello.Feature.Order.Repository.OrderItemRepository;
import com.example.hello.Feature.Order.dto.OrderItemInfo;
import com.example.hello.Feature.ProductsManager.Repository.ProductRepository;
import com.example.hello.Feature.ProductsManager.Repository.VariantRepository;
import com.example.hello.Feature.ProductsManager.dto.VariantInfo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductAsyncTask {
    ProductRepository productRepository;
    OrderItemRepository orderItemRepository;
    VariantRepository variantRepository;

    @Async
    public void countTotalSalesByProductIds(List<UUID> variantIds) {
        var productIds = productRepository.findByVariantIds(variantIds);
        var products = productRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));
        var variants = variantRepository.findVariantInfoByProductIds(productIds)
                .stream()
                .collect(Collectors.groupingBy(VariantInfo::getProductId));
        productIds.forEach(productId -> {
            var sold = variants.get(productId)
                    .stream()
                    .mapToInt(VariantInfo::getSold)
                    .sum();
            products.get(productId).setTotalSales(sold);

        });
        productRepository.saveAll(products.values());
        log.info("Updated total sales by product ids: {}", productIds);
    }

    @Async
    public void updateProductWhenCancel(List<UUID> orderIds) {
        var variantInfos = orderItemRepository.getOrderItemsVariant(orderIds);
        var variantInfosGroup = variantInfos.stream()
                .collect(Collectors
                        .toMap(OrderItemInfo::getVariantId, Function.identity()));
        var variantIds = variantInfos.stream()
                .map(OrderItemInfo::getVariantId)
                .distinct()
                .toList();
        var variants = variantRepository.findAllById(variantIds);
        variants.forEach(variant -> {
            variant.setSold(variant.getSold() -
                    variantInfosGroup.get(variant.getVariantId()).getQuantity());
            variant.setStock(variant.getStock() +
                    variantInfosGroup.get(variant.getVariantId()).getQuantity());
        });
        variantRepository.saveAll(variants);
        log.info("Updated variant when cancel successfully");
        countTotalSalesByProductIds(variantIds);
    }
}

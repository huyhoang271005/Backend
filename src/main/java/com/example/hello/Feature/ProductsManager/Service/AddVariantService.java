package com.example.hello.Feature.ProductsManager.Service;

import com.example.hello.Entity.*;
import com.example.hello.Feature.ProductsManager.dto.*;
import com.example.hello.Infrastructure.Cloudinary.CloudinaryResponse;
import com.example.hello.Infrastructure.Cloudinary.CloudinaryService;
import com.example.hello.Infrastructure.Cloudinary.FolderCloudinary;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Mapper.VariantMapper;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Feature.Attribute.AttributeValueRepository;
import com.example.hello.Feature.ProductsManager.Repository.VariantRepository;
import com.example.hello.Feature.ProductsManager.Repository.VariantValueRepository;
import com.example.hello.Feature.Attribute.AttributeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AddVariantService {
    AttributeValueRepository attributeValueRepository;
    VariantRepository variantRepository;
    VariantValueRepository variantValueRepository;
    AttributeRepository attributeRepository;
    CloudinaryService cloudinaryService;
    VariantMapper variantMapper;
    Executor virtualThreadExecutor;

    @Transactional
    public void processAttributesAndVariants(Product product,
                                             ProductDTO productDTO,
                                             Map<String, MultipartFile> images) {
        // STEP 1: Create and save AttributeValues
        Map<String, AttributeValue> attributeValueMap = createAndSaveAttributeValues(
                product, productDTO);

        // STEP 2: Create and save Variants
        Map<String, Variant> variantMap = createAndSaveVariants(
                product, productDTO, images);

        // STEP 3: Create and save VariantValues (junction table)
        createAndSaveVariantValues(productDTO, attributeValueMap, variantMap);
    }

    private Map<String, AttributeValue> createAndSaveAttributeValues(
            Product product, ProductDTO productDTO) {

        List<AttributeValue> attributeValues = new ArrayList<>();
        Map<String, AttributeValue> attributeValueMap = new HashMap<>();

        for (AttributeDTO attributeDTO : productDTO.getAttributes()) {
            // Get attribute from DB
            Attribute attribute = attributeRepository
                    .findById(attributeDTO.getAttributeId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            StringApplication.FIELD.ATTRIBUTE + StringApplication.FIELD.NOT_EXIST));

            // Create AttributeValues for this attribute
            for (AttributeValueDTO valueDTO : attributeDTO.getAttributeValues()) {
                AttributeValue attributeValue = AttributeValue.builder()
                        .product(product)
                        .attribute(attribute)
                        .value(valueDTO.getAttributeValueName())
                        .build();

                attributeValues.add(attributeValue);
                // Map temp ID để dùng sau
                attributeValueMap.put(valueDTO.getAttributeValueId(), attributeValue);
            }
        }

        // SAVE tất cả AttributeValues một lần
        attributeValueRepository.saveAll(attributeValues);
        log.info("Attribute values successfully saved");
        return attributeValueMap;
    }

    private Map<String, Variant> createAndSaveVariants(
            Product product,
            ProductDTO productDTO,
            Map<String, MultipartFile> images) {

        List<CompletableFuture<Map.Entry<String, Variant>>> futures = productDTO.getVariants()
                .stream()
                .map(variantDTO -> CompletableFuture.supplyAsync(() -> {
                    CloudinaryResponse cloudinaryResponse = cloudinaryService
                            .uploadImage(images.get(variantDTO.getImageName()),
                            FolderCloudinary.variant.name());
                    Variant variant = new Variant();
                    variantMapper.updateVariant(variantDTO, variant);
                    variant.setImageId(cloudinaryResponse.getPublicId());
                    variant.setImageUrl(cloudinaryResponse.getUrl());
                    variant.setProduct(product);
                    variant.setActive(true);

                    return Map.entry(variantDTO.getVariantId(), variant);
                }, virtualThreadExecutor))
                .toList();
        try {
            List<Map.Entry<String, Variant>> result = futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .toList();


            variantRepository.saveAll(result.stream()
                    .map(Map.Entry::getValue)
                    .toList());

            log.info("Variant successfully saved");

            return result.stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (variant, variant2) -> variant));
        } catch (Exception e) {
            log.error("Error while saving variants", e);
            var imageIds = futures.stream()
                    .filter(variantCompletableFuture ->
                            variantCompletableFuture.isDone() && !variantCompletableFuture.isCompletedExceptionally())
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .map(stringVariantEntry -> stringVariantEntry.getValue().getImageId())
                    .toList();

            log.info("Clean image {} uploaded", imageIds);
            CompletableFuture.runAsync(() -> cloudinaryService.deleteImages(imageIds));
            throw new RuntimeException(e.getMessage());
        }
    }

    private void createAndSaveVariantValues(
            ProductDTO productDTO,
            Map<String, AttributeValue> attributeValueMap,
            Map<String, Variant> variantMap) {

        List<VariantValue> variantValues = new ArrayList<>();

        for (VariantValueDTO variantValueDTO : productDTO.getVariantValues()) {
            AttributeValue attributeValue = attributeValueMap.get(
                    variantValueDTO.getAttributeValueId());
            Variant variant = variantMap.get(variantValueDTO.getVariantId());

            if (attributeValue == null || variant == null) {
                continue;
            }

            VariantValue variantValue = VariantValue.builder()
                    .variant(variant)
                    .attributeValue(attributeValue)
                    .build();

            variantValues.add(variantValue);
        }
        variantValueRepository.saveAll(variantValues);
        log.info("Variant values successfully saved");
    }
}
package com.example.hello.Feature.ProductsManager.Service;

import com.example.hello.Entity.*;
import com.example.hello.Feature.ProductsManager.dto.*;
import com.example.hello.Infrastructure.Cloudinary.CloudinaryResponse;
import com.example.hello.Infrastructure.Cloudinary.CloudinaryService;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

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

    @Transactional
    public void processAttributesAndVariants(Product product,
                                             ProductDTO productDTO,
                                             Map<String, MultipartFile> images) {
        List<String> uploadedImageIds = new ArrayList<>();
        try {
            // STEP 1: Create and save AttributeValues
            Map<String, AttributeValue> attributeValueMap = createAndSaveAttributeValues(
                    product, productDTO);

            // STEP 2: Create and save Variants
            Map<String, Variant> variantMap = createAndSaveVariants(
                    product, productDTO, images, uploadedImageIds);

            // STEP 3: Create and save VariantValues (junction table)
            createAndSaveVariantValues(productDTO, attributeValueMap, variantMap);

        } catch (Exception e) {
            cleanupUploadedImages(uploadedImageIds);
            log.error(String.valueOf(e));
            throw new RuntimeException(e.getMessage());
        }
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
            Map<String, MultipartFile> images,
            List<String> uploadedImageIds) {

        List<Variant> variants = new ArrayList<>();
        Map<String, Variant> variantMap = new HashMap<>();

        for (VariantDTO variantDTO : productDTO.getVariants()) {
            MultipartFile imageFile = images.get(variantDTO.getImageName());

            if (imageFile == null) {
                log.error("{} image not found", variantDTO.getImageName());
                continue;
            }

            // Upload image to Cloudinary
            CloudinaryResponse imageVariant = cloudinaryService
                    .uploadImage(imageFile, "variant");
            uploadedImageIds.add(imageVariant.getPublicId());

            // Create Variant
            Variant variant = new Variant();
            variantMapper.updateVariant(variantDTO, variant);
            variant.setImageId(imageVariant.getPublicId());
            variant.setImageUrl(imageVariant.getUrl());
            variant.setProduct(product);
            variant.setActive(true);

            variants.add(variant);
            variantMap.put(variantDTO.getVariantId(), variant);
        }

        variantRepository.saveAll(variants);
        log.info("Variant successfully saved");
        return variantMap;
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

    @Async
    public void cleanupUploadedImages(List<String> publicIds) {
        if (publicIds.isEmpty()) {
            return;
        }
        publicIds.forEach(s -> {
                log.info("Image {} deleted", s);
                cloudinaryService.deleteImage(s);
        });
        log.info("Image failure successfully cleaned");
    }
}
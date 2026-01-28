package com.example.hello.Feature.ProductsManager.Service;

import com.example.hello.Entity.*;
import com.example.hello.Feature.ProductsManager.DTO.*;
import com.example.hello.Infrastructure.Cloudinary.CloudinaryResponse;
import com.example.hello.Infrastructure.Cloudinary.CloudinaryService;
import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Infrastructure.Exception.UnprocessableEntityException;
import com.example.hello.Mapper.ProductMapper;
import com.example.hello.Mapper.VariantMapper;
import com.example.hello.Middleware.Response;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductService {
    ProductRepository productRepository;
    CategoryRepository categoryRepository;
    BrandRepository brandRepository;
    CloudinaryService cloudinaryService;
    AddVariantService addVariantService;
    ProductMapper productMapper;
    VariantMapper variantMapper;
    VariantRepository variantRepository;

    private void checkProductDTO(ProductDTO productDTO, Map<String, MultipartFile> images) {
        productDTO.getVariants().forEach(variant -> {
            if(images.get(variant.getImageName()) == null) {
                log.error("Not found image name {}", variant.getImageName());
                throw new UnprocessableEntityException("Variant " + variant.getImageName() +" not found");
            }
        });
        var attributeValueSize = productDTO.getAttributes().stream()
                .map(attributeDTO -> attributeDTO.getAttributeValues().size())
                .reduce(1, (a, b) -> a * b);
        if(attributeValueSize != productDTO.getVariants().size()) {
            log.error("attribute values are not equal variants size");
            throw new UnprocessableEntityException("AttributeValue and Variant not match");
        }
        Map<UUID, Set<String>> attributeValueMap =
                productDTO.getAttributes().stream()
                        .collect(Collectors.toMap(
                                AttributeDTO::getAttributeId,
                                a -> a.getAttributeValues().stream()
                                        .map(AttributeValueDTO::getAttributeValueId)
                                        .collect(Collectors.toSet())
                        ));
        var variantValuesById =
                productDTO.getVariantValues().stream()
                        .collect(Collectors.groupingBy(VariantValueDTO::getVariantId));
        log.info("Group by variant values successfully");

        for (var variant : productDTO.getVariants()) {
            var values = variantValuesById.get(variant.getVariantId());

            if (values == null || values.size() != productDTO.getAttributes().size()) {
                log.error("Variant size mismatch attribute values");
                throw new UnprocessableEntityException("Variant missing attribute values");
            }

            Set<UUID> usedAttributes = new HashSet<>();

            for (var vv : values) {
                boolean matched = false;

                for (var entry : attributeValueMap.entrySet()) {
                    if (entry.getValue().contains(vv.getAttributeValueId())) {
                        if (!usedAttributes.add(entry.getKey())) {
                            log.error("Attribute values duplicated {}", entry.getKey());
                            throw new UnprocessableEntityException("Duplicate attribute in variant");
                        }
                        matched = true;
                        break;
                    }
                }

                if (!matched) {
                    throw new UnprocessableEntityException("Invalid attribute value");
                }
            }
        }
    }
    @Transactional
    public Response<Product> addProduct(ProductDTO productDTO, Map<String, MultipartFile> images) {
        checkProductDTO(productDTO, images);
        if(images.get("productImage") == null) {
            log.error("Not found product image");
            throw new UnprocessableEntityException("Product Image not found");
        }
        //Lấy dữ liệu category và brand
        Category category = categoryRepository.findById(productDTO.getProductDetailDTO().getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException(StringApplication.FIELD.CATEGORY +
                        StringApplication.FIELD.NOT_EXIST));
        log.info("Foud category successfully");
        Brand brand = brandRepository.findById(productDTO.getProductDetailDTO().getBrandId())
                .orElseThrow(() -> new EntityNotFoundException(StringApplication.FIELD.BRAND +
                        StringApplication.FIELD.NOT_EXIST));
        log.info("Foud brand successfully");
        //upload product
        CloudinaryResponse imageProduct = cloudinaryService
                .uploadImage(images.get("productImage"), "product");
        //Tạo và lưu sản phẩm
        Product product = new Product();
        productMapper.updateProduct(productDTO.getProductDetailDTO(), product);
        product.setCategory(category);
        product.setBrand(brand);
        product.setImageId(imageProduct.getPublicId());
        product.setImageUrl(imageProduct.getUrl());
        productRepository.save(product);
        log.info("Product successfully added successfully");

        //Tạo và lưu các biến thể
        addVariantService.processAttributesAndVariants(product, productDTO, images);
        return new Response<>(true,
                StringApplication.FIELD.SUCCESS,
                null);

    }

    @Transactional(readOnly = true)
    public Response<ProductDTO> getProduct(UUID productId) {
        //Get product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException(
                        StringApplication.FIELD.PRODUCT + StringApplication.FIELD.NOT_EXIST));

        log.info("Foud product successfully");
        //Build product detail
        ProductDetailDTO productDetail = productMapper.toProductDTO(product);
        productDetail.setCategoryId(product.getCategory().getCategoryId());
        productDetail.setBrandId(product.getBrand().getBrandId());
        //Group AttributeValues by Attribute
        Map<Attribute, List<AttributeValue>> attributeMap = product.getAttributeValues()
                .stream()
                .collect(Collectors.groupingBy(AttributeValue::getAttribute));

        //Build attributes list
        List<AttributeDTO> attributes = attributeMap.entrySet().stream()
                .map(entry -> {
                    Attribute attribute = entry.getKey();
                    List<AttributeValue> values = entry.getValue();

                    return AttributeDTO.builder()
                            .attributeId(attribute.getAttributeId())
                            .attributeName(attribute.getAttributeName())
                            .attributeValues(values.stream()
                                    .map(av -> AttributeValueDTO.builder()
                                            .attributeValueId(String.valueOf(av.getAttributeValueId()))
                                            .attributeValueName(av.getValue())
                                            .build())
                                    .toList())
                            .build();
                })
                .toList();

        // Build variants list
        var variantCurrent = product.getVariants();
        log.info("Foud variant successfully");
        List<VariantDTO> variants = variantCurrent.stream()
                .map(variantMapper::toVariantDTO)
                .toList();

        //Build VariantValues
        List<VariantValueDTO> variantValuesDTO = new ArrayList<>();
        variantCurrent.forEach(variant -> {
            var v = variant.getVariantValues();
            var variantValue = v.stream().map(vv -> VariantValueDTO.builder()
                    .variantId(String.valueOf(variant.getVariantId()))
                    .attributeValueId(String.valueOf(vv.getAttributeValue().getAttributeValueId()))
                    .build())
                    .toList();
            variantValuesDTO.addAll(variantValue);
        });
        log.info("Mapping variant values successfully");
        //Build complete response
        ProductDTO response = ProductDTO.builder()
                .productDetailDTO(productDetail)
                .attributes(attributes)
                .variantValues(variantValuesDTO)
                .variants(variants)
                .build();

        log.info("Response successfully");

        return new Response<>(true, StringApplication.FIELD.SUCCESS, response);
    }

    @Transactional
    public Response<Void> updateProduct(ProductDetailDTO productDetailDTO, MultipartFile image) {
        var product =  productRepository.findById(productDetailDTO.getProductId())
                .orElseThrow(() -> new EntityNotFoundException(StringApplication.FIELD.PRODUCT +
                        StringApplication.FIELD.NOT_EXIST));
        productMapper.updateProduct(productDetailDTO, product);
        if(image != null) {
            if(product.getImageId() != null) {
                log.info("Image product not null");
                cloudinaryService.deleteImage(product.getImageId());
            }
            var imageCurrent = cloudinaryService.uploadImage(image, "product");
            product.setImageUrl(imageCurrent.getUrl());
            product.setImageId(imageCurrent.getPublicId());
        }
        return new Response<>(true, StringApplication.FIELD.SUCCESS, null);
    }

    @Transactional
    public Response<Void> deleteProduct(UUID productId) {
        var product = productRepository.findById(productId).orElseThrow(() -> new EntityNotFoundException(
                StringApplication.FIELD.PRODUCT + StringApplication.FIELD.NOT_EXIST
        ));
        if(product.getImageId() != null) {
            cloudinaryService.deleteImage(product.getImageId());
        }
        product.getVariants().forEach(variant -> {
            if(variant.getImageId() != null) {
                cloudinaryService.deleteImage(variant.getImageId());
            }
        });
        variantRepository.deleteAll(product.getVariants());
        log.info("Variant in product {} successfully deleted", productId);
        productRepository.delete(product);
        log.info("Product successfully deleted");
        return new Response<>(true, StringApplication.FIELD.SUCCESS, null);
    }

    @Transactional
    public Response<Void> addVariant(ProductDTO productDTO, Map<String, MultipartFile> images) {
        checkProductDTO(productDTO, images);
        var product = productRepository.findById(productDTO.getProductDetailDTO().getProductId())
                        .orElseThrow(() -> new EntityNotFoundException(StringApplication.FIELD.PRODUCT +
                                StringApplication.FIELD.NOT_EXIST));
        addVariantService.processAttributesAndVariants(product, productDTO, images);
        return new Response<>(true, StringApplication.FIELD.SUCCESS, null);
    }


    @Transactional
    public Response<Void> updateVariant(VariantDTO variantDTO, MultipartFile image){
        var variant = variantRepository.findById(UUID.fromString(variantDTO.getVariantId()))
                .orElseThrow(() -> new EntityNotFoundException(StringApplication.FIELD.PRODUCT +
                        StringApplication.FIELD.NOT_EXIST));
        variantMapper.updateVariant(variantDTO, variant);
        if(image != null) {
            if(variant.getImageId() != null){
                cloudinaryService.deleteImage(variant.getImageId());
            }
            var imageCurrent = cloudinaryService.uploadImage(image, "variant");
            variant.setImageUrl(imageCurrent.getUrl());
            variant.setImageId(imageCurrent.getPublicId());
        }
        log.info("Variant successfully updated");
        return new Response<>(true, StringApplication.FIELD.SUCCESS, null);
    }

    @Transactional
    public Response<Void> deleteVariant(UUID variantId) {
        var variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new EntityNotFoundException(StringApplication.FIELD.PRODUCT +
                        StringApplication.FIELD.NOT_EXIST));
        if(variant.getSold() > 0){
            throw new ConflictException(StringApplication.FIELD.CANT_REMOVE);
        }
        if(variant.getImageId() != null) {
            cloudinaryService.deleteImage(variant.getImageId());
        }
        variantRepository.delete(variant);
        log.info("Variant {} successfully deleted", variantId);
        return new Response<>(true, StringApplication.FIELD.SUCCESS, null);
    }
}
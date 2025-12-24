package com.example.hello.Feature.ProductsManager.Controller;

import com.example.hello.Feature.ProductsManager.DTO.ProductDTO;
import com.example.hello.Feature.ProductsManager.DTO.ProductDetailDTO;
import com.example.hello.Feature.ProductsManager.DTO.VariantDTO;
import com.example.hello.Feature.ProductsManager.Service.ProductService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("auth/admin/products")
public class ProductController {
    ProductService productService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProduct(@Valid @RequestPart ProductDTO productDTO,
                                        @RequestParam Map<String, MultipartFile> images) {
        return ResponseEntity.ok(productService.addProduct(productDTO, images));
    }

    @GetMapping("{productId}")
    public ResponseEntity<?> getAllProducts(@PathVariable UUID productId) {
        return ResponseEntity.ok(productService.getProduct(productId));
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduct(@Valid @RequestPart ProductDetailDTO productDetailDTO,
                                           @RequestPart MultipartFile image) {
        return ResponseEntity.ok(productService.updateProduct(productDetailDTO, image));
    }

    @DeleteMapping("{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable UUID productId) {
        return  ResponseEntity.ok(productService.deleteProduct(productId));
    }

    @PostMapping(value = "variants", consumes =  MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addVariants(@Valid @RequestPart ProductDTO productDTO,
                                           @RequestParam Map<String, MultipartFile> images){
        return ResponseEntity.ok(productService.addVariant(productDTO, images));
    }

    @PutMapping(value = "variants", consumes =   MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateVariant(@RequestPart VariantDTO variantDTO,
                                           @RequestPart MultipartFile image){
        return ResponseEntity.ok(productService.updateVariant(variantDTO, image));
    }

    @DeleteMapping("variants/{variantId}")
    public ResponseEntity<?> deleteVariant(@PathVariable UUID variantId){
        return ResponseEntity.ok(productService.deleteVariant(variantId));
    }
}

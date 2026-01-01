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
import org.springframework.security.access.prepost.PreAuthorize;
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

//    @PreAuthorize("hasAuthority('ADD_PRODUCT')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProduct(@Valid @RequestPart ProductDTO productDTO,
                                        @RequestParam Map<String, MultipartFile> images) {
        return ResponseEntity.ok(productService.addProduct(productDTO, images));
    }

//    @PreAuthorize("hasAuthority('GET_PRODUCT_ADMIN')")
    @GetMapping("{productId}")
    public ResponseEntity<?> getAllProducts(@PathVariable UUID productId) {
        return ResponseEntity.ok(productService.getProduct(productId));
    }

//    @PreAuthorize("hasAuthority('UPDATE_PRODUCT')")
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduct(@Valid @RequestPart ProductDetailDTO productDetailDTO,
                                           @RequestPart MultipartFile image) {
        return ResponseEntity.ok(productService.updateProduct(productDetailDTO, image));
    }

//    @PreAuthorize("hasAuthority('DELETE_PRODUCT')")
    @DeleteMapping("{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable UUID productId) {
        return  ResponseEntity.ok(productService.deleteProduct(productId));
    }

//    @PreAuthorize("hasAuthority('ADD_VARIANT')")
    @PostMapping(value = "variants", consumes =  MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addVariants(@Valid @RequestPart ProductDTO productDTO,
                                           @RequestParam Map<String, MultipartFile> images){
        return ResponseEntity.ok(productService.addVariant(productDTO, images));
    }

//    @PreAuthorize("hasAuthority('UPDATE_VARIANT')")
    @PutMapping(value = "variants", consumes =   MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateVariant(@RequestPart VariantDTO variantDTO,
                                           @RequestPart MultipartFile image){
        return ResponseEntity.ok(productService.updateVariant(variantDTO, image));
    }

//    @PreAuthorize("hasAuthority('DELETE_VARIANT')")
    @DeleteMapping("variants/{variantId}")
    public ResponseEntity<?> deleteVariant(@PathVariable UUID variantId){
        return ResponseEntity.ok(productService.deleteVariant(variantId));
    }
}

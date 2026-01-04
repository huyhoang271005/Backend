package com.example.hello.Feature.ProductDetail.Controller;

import com.example.hello.Feature.ProductDetail.Service.ProductDetailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("auth/products")
public class ProductDetailController {
    ProductDetailService productDetailService;

    @GetMapping("{productId}")
    public ResponseEntity<?> getProductDetail(@PathVariable UUID productId) {
        return ResponseEntity.ok(productDetailService.getProductDetail(productId));
    }

    @GetMapping
    public ResponseEntity<?> getProducts(@RequestParam(required = false) String productName,
                                         @RequestParam(required = false) UUID categoryId,
                                         @RequestParam(required = false) UUID brandId,
                                         @RequestParam(required = false) BigDecimal minPrice,
                                         @RequestParam(required = false) BigDecimal maxPrice,
                                         Pageable pageable) {
        return ResponseEntity.ok(productDetailService.getProductList(productName,
                categoryId, brandId,
                minPrice, maxPrice, pageable));
    }
}

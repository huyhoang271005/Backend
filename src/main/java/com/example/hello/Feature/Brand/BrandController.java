package com.example.hello.Feature.Brand;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("brands")
public class BrandController {
    BrandService brandService;

    @PreAuthorize("hasAuthority('ADD_BRAND')")
    @PostMapping
    public ResponseEntity<?> addBrand(@RequestBody BrandDTO brandDTO) {
        return ResponseEntity.ok(brandService.addBrand(brandDTO));
    }

    @GetMapping
    public ResponseEntity<?> getBrands(Pageable pageable) {
        return ResponseEntity.ok(brandService.getAllBrands(pageable));
    }

    @PreAuthorize("hasAuthority('UPDATE_BRAND')")
    @PutMapping
    public ResponseEntity<?> updateBrand(@RequestBody BrandDTO brandDTO) {
        return ResponseEntity.ok(brandService.updateBrand(brandDTO));
    }

    @PreAuthorize("hasAuthority('DELETE_BRAND')")
    @DeleteMapping("{brandId}")
    public ResponseEntity<?> deleteBrand(@PathVariable UUID brandId) {
        return ResponseEntity.ok(brandService.deleteBrand(brandId));
    }
}

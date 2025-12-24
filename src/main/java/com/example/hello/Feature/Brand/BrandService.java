package com.example.hello.Feature.Brand;

import com.example.hello.Entity.Brand;
import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Middleware.ListResponse;
import com.example.hello.Middleware.Response;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Repository.BrandRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BrandService {
    BrandRepository brandRepository;

    @Transactional
    public Response<BrandDTO> addBrand(BrandDTO brandDTO) {
        var brand = brandRepository.existsByBrandName(brandDTO.getBrandName());
        if(brand) {
            throw new ConflictException(StringApplication.FIELD.BRAND + StringApplication.FIELD.EXISTED);
        }
        brandRepository.save(Brand.builder()
                .brandName(brandDTO.getBrandName())
                .description(brandDTO.getDescription())
                .build());
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                BrandDTO.builder()
                        .brandId(brandDTO.getBrandId())
                        .brandName(brandDTO.getBrandName())
                        .description(brandDTO.getDescription())
                        .build()
        );
    }

    @Transactional(readOnly = true)
    public Response<ListResponse<BrandDTO>> getAllBrands(Pageable pageable) {
        var list = brandRepository.findAllByPageable(pageable);
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                new ListResponse<>(
                        list.hasNext(),
                        list.getContent().stream()
                                .map(brandInfo -> BrandDTO.builder()
                                        .brandId(brandInfo.getBrandId())
                                        .brandName(brandInfo.getBrandName())
                                        .description(brandInfo.getDescription())
                                        .build())
                                .toList()
                )
        );
    }

    @Transactional
    public Response<Void> updateBrand(BrandDTO brandDTO) {
        if(!brandRepository.existsById(brandDTO.getBrandId())){
            throw new ConflictException(StringApplication.FIELD.BRAND + StringApplication.FIELD.NOT_EXIST);
        }
        brandRepository.save(Brand.builder()
                .brandId(brandDTO.getBrandId())
                .brandName(brandDTO.getBrandName())
                .description(brandDTO.getDescription())
                .build());
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }

    @Transactional
    public Response<Void> deleteBrand(UUID brandId) {
        var brand = brandRepository.findById(brandId).orElseThrow(
                ()-> new EntityNotFoundException(StringApplication.FIELD.BRAND + StringApplication.FIELD.NOT_EXIST)
        );
        brandRepository.delete(brand);
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }
}

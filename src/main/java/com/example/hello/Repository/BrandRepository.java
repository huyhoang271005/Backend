package com.example.hello.Repository;

import com.example.hello.Feature.Authentication.DataProjection.BrandInfo;
import com.example.hello.Entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface BrandRepository extends JpaRepository<Brand, UUID> {
    Boolean existsByBrandName(String brandName);
    @Query("""
            select b.brandId as brandId, b.brandName as brandName, b.description as description
            from Brand b
            order by b.updatedAt desc
            """)
    Page<BrandInfo> findAllByPageable(Pageable pageable);
}
package com.example.hello.Repository;

import com.example.hello.Entity.Variant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VariantRepository extends JpaRepository<Variant, UUID> {
    List<Variant> findByVariantIdIn(List<UUID> variantIds);
}
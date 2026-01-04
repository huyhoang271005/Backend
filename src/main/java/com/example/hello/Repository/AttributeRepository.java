package com.example.hello.Repository;

import com.example.hello.Feature.Authentication.DataProjection.AttributeInfo;
import com.example.hello.Entity.Attribute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface AttributeRepository extends JpaRepository<Attribute, UUID> {
    Boolean existsByAttributeName(String attributeName);
    @Query("""
            select a
            from Attribute a
            order by a.updatedAt desc
            """)
    Page<AttributeInfo> findAllByPageable(Pageable pageable);
}
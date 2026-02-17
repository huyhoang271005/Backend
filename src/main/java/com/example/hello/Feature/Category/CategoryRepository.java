package com.example.hello.Feature.Category;

import com.example.hello.Entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Boolean existsByCategoryName(String categoryName);
    @Query("""
            select c
                from Category c
            order by c.updatedAt desc
            """)
    Page<Category> getAll(Pageable pageable);
}
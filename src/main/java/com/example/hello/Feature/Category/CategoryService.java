package com.example.hello.Feature.Category;

import com.example.hello.Entity.Category;
import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Middleware.ListResponse;
import com.example.hello.Middleware.Response;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Feature.Category.CategoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryService {
    CategoryRepository categoryRepository;

    @Transactional
    public Response<CategoryDTO> addCategory(CategoryDTO categoryDTO) {
        var result = categoryRepository.existsByCategoryName(categoryDTO.getCategoryName());
        if(result) {
            throw new ConflictException(StringApplication.FIELD.CATEGORY + StringApplication.FIELD.EXISTED);
        }
        log.info("Adding new category successfully");
        categoryRepository.save(Category.builder()
                .categoryId(categoryDTO.getCategoryId())
                .categoryName(categoryDTO.getCategoryName())
                .description(categoryDTO.getDescription())
                .build());
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                CategoryDTO.builder()
                        .categoryId(categoryDTO.getCategoryId())
                        .categoryName(categoryDTO.getCategoryName())
                        .description(categoryDTO.getDescription())
                        .build()
        );
    }

    @Transactional(readOnly = true)
    public Response<ListResponse<CategoryDTO>> findAllCategories(Pageable pageable) {
        var categories = categoryRepository.getAll(pageable);
        log.info("Finding all categories successfully");
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                new ListResponse<>(
                        categories.hasNext(),
                        categories.getContent().stream()
                                .map(category -> CategoryDTO.builder()
                                        .categoryId(category.getCategoryId())
                                        .categoryName(category.getCategoryName())
                                        .description(category.getDescription())
                                        .build())
                                .toList()
                )
        );
    }

    @Transactional
    public Response<Void> updateCategory(CategoryDTO categoryDTO) {
        var category = categoryRepository.findById(categoryDTO.getCategoryId()).orElseThrow(
                ()-> new ConflictException(StringApplication.FIELD.CATEGORY + StringApplication.FIELD.NOT_EXIST)
        );
        category.setDescription(categoryDTO.getDescription());
        category.setCategoryName(categoryDTO.getCategoryName());
        log.info("Update category successfully");
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }

    @Transactional
    public Response<Void> deleteCategory(UUID categoryId) {
        var category = categoryRepository.findById(categoryId).orElseThrow(
                ()-> new  EntityNotFoundException(StringApplication.FIELD.CATEGORY + StringApplication.FIELD.NOT_EXIST)
        );
        categoryRepository.delete(category);
        log.info("Remove category successfully");
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }
}

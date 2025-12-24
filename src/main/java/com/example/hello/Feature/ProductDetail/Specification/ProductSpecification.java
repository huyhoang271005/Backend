package com.example.hello.Feature.ProductDetail.Specification;

import com.example.hello.Entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductSpecification {
    public static Specification<Product> hasCategory(UUID categoryId){
        return (root, criteriaQuery, criteriaBuilder) ->
        {
            if(categoryId == null){
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("category").get("categoryId"), categoryId);
        };
    }

    public static Specification<Product> hasBrand(UUID brandId){
        return (root, criteriaQuery, criteriaBuilder) ->
        {
            if(brandId == null){
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("brand").get("brandId"), brandId);
        };
    }

    public static Specification<Product> betweenPrice(BigDecimal minPrice, BigDecimal maxPrice){
        return (root, criteriaQuery, criteriaBuilder) ->
        {
            if (minPrice == null && maxPrice == null) {
                return criteriaBuilder.conjunction();
            }

            if (minPrice != null && maxPrice != null) {
                return criteriaBuilder.between(root.get("price"), minPrice, maxPrice);
            }

            if (minPrice != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
            }

            return criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }
}

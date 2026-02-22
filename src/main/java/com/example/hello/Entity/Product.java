package com.example.hello.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "product", indexes = {
        @Index(name = "idx_product_brand_id", columnList = "brand_id"),
        @Index(name = "idx_product_category_id", columnList = "category_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "product_id")
    UUID productId;

    @Column(name = "product_name", columnDefinition = "NVARCHAR(255)")
    String productName;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    String description;

    @Column(name = "image_id", columnDefinition = "VARCHAR(255)")
    String imageId;

    @Column(name = "image_url", columnDefinition = "VARCHAR(255)")
    String imageUrl;

    @Column(name = "original_price")
    BigDecimal originalPrice;

    BigDecimal price;

    @Column(name = "total_sales")
    @Builder.Default
    Integer totalSales = 0;

    @Column(name = "rating_avg")
    @Builder.Default
    Double ratingAvg = 0.0;

    @Column(name = "rating_count")
    @Builder.Default
    Integer ratingCount = 0;

    @Column(name = "created_at")
    @CreationTimestamp
    Instant createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    Category category;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product", cascade = CascadeType.ALL)
    List<AttributeValue> attributeValues;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product")
    List<Variant> variants;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product")
    List<Feedback> feedbacks;
}

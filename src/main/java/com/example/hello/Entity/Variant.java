package com.example.hello.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "variant", indexes = {
        @Index(name = "idx_variant_product_id", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Variant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "variant_id")
    UUID variantId;

    @Column(name = "original_price")
    BigDecimal originalPrice;

    @Column(name = "price")
    BigDecimal price;

    Integer stock;

    @Builder.Default
    Integer sold = 0;

    @Column(name = "image_id", columnDefinition = "VARCHAR(255)")
    String imageId;

    @Column(name = "image_url", columnDefinition = "VARCHAR(255)")
    String imageUrl;

    @Column(name = "is_active")
    Boolean active;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "variant", cascade = CascadeType.MERGE)
    List<VariantValue> variantValues;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    Product product;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "variant", cascade = CascadeType.ALL)
    List<CartItem> cartItems;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "variant", cascade = CascadeType.MERGE)
    List<OrderItem> orderItems;
}

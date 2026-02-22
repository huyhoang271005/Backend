package com.example.hello.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "attribute_value", indexes = {
        @Index(name = "idx_attribute_value_attribute_id", columnList = "attribute_id"),
        @Index(name = "idx_attribute_value_product_id", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttributeValue {
    @Id
    @Column(name = "attribute_value_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID attributeValueId;

    @Column(columnDefinition = "NVARCHAR(255)")
    String value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id")
    Attribute attribute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    Product product;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "attributeValue", cascade = CascadeType.ALL)
    List<VariantValue> variantValues;
}

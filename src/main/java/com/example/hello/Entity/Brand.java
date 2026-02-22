package com.example.hello.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "brand")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "brand_id")
    UUID brandId;

    @Column(name = "brand_name", unique = true, columnDefinition = "NVARCHAR(255)")
    String brandName;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    String description;

    @Column(name = "updated_at")
    @UpdateTimestamp
    Instant updatedAt;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "brand", cascade = CascadeType.MERGE)
    List<Product> products;
}

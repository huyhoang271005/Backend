package com.example.hello.Entity;

import com.example.hello.Enum.CartStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cart", indexes = {
        @Index(name = "idx_cart_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cart_id")
    UUID cartId;

    @Enumerated(EnumType.STRING)
    @Column(name = "cart_status")
    @Builder.Default
    CartStatus cartStatus =  CartStatus.ACTIVE;

    @Column(name = "created_at")
    @CreationTimestamp
    Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    User user;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "cart", cascade = CascadeType.ALL)
    List<CartItem> cartItems;
}

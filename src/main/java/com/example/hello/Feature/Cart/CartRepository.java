package com.example.hello.Feature.Cart;

import com.example.hello.Entity.Cart;
import com.example.hello.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByUser(User user);
    @Query("""
            select c
            from CartItem ci
            join ci.cart c
            join c.user u
            where ci.cartItemId = :cartItemId and u.userId = :userId
            """)
    Optional<Cart> findByCartItemIdAndUserId(UUID cartItemId, UUID userId);
}
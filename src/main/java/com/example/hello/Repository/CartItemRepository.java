package com.example.hello.Repository;

import com.example.hello.DataProjection.CartItemInfo;
import com.example.hello.DataProjection.VariantInfo;
import com.example.hello.DataProjection.ProductInfo;
import com.example.hello.Entity.Cart;
import com.example.hello.Entity.CartItem;
import com.example.hello.Entity.Variant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    @Query("""
            select p.productId as productId,
                    v.imageUrl as imageUrl, v.active as active, v.variantId as variantId,
                    v.price as price, v.originalPrice as originalPrice, v.stock as stock
            from Product p
            join p.variants v
            where v.product.productId in :listProductId
           \s""")
    List<VariantInfo> getProductVariants(List<UUID> listProductId);

    @Query("""
            select p.productId as productId, p.productName as productName
            from CartItem ci
            join ci.variant.product p
            where ci.cart.user.userId = :userId
            order by ci.updatedAt desc
            """)
    Page<ProductInfo> getProductIds(UUID userId, Pageable pageable);

    Optional<CartItem> findByCartAndVariant(Cart cart, Variant variant);

    @EntityGraph(attributePaths = {"cart", "cart.user"})
    Optional<CartItem> findByCartItemId(UUID cartItemId);

    Integer countByCart(Cart cart);

    @Query("""
            select p.productId as productId, v.variantId as variantId,
            ci.cartItemId as cartItemId, ci.oldPrice as oldPrice, ci.quantity as quantity
            from CartItem ci
            join ci.variant v
            join v.product p
            where p.productId in :listProductId
            """)
    List<CartItemInfo> getCartItemByProductId(List<UUID> listProductId);

    @Modifying
    void deleteByCart_User_UserIdAndCartItemIdIn(UUID cartUserUserId, List<UUID> cartItemIds);

    @Modifying
    void deleteByCartItemIdIn(List<UUID> cartItemIds);

    Integer countByCart_User_UserId(UUID userId);
}
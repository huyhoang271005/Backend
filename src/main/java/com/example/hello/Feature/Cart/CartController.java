package com.example.hello.Feature.Cart;

import com.example.hello.Feature.Cart.CartDTO.CartItemDTO;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("carts")
public class CartController {
    CartService cartService;
    @PostMapping
    public ResponseEntity<?> addCartItem(@AuthenticationPrincipal UUID userId,
                                         @Valid @RequestBody CartItemDTO cartItemDTO){
        return ResponseEntity.ok(cartService.addCartItem(userId, cartItemDTO));
    }

    @GetMapping
    public ResponseEntity<?> getCartItems(@AuthenticationPrincipal UUID userId,
                                          Pageable pageable){
        return ResponseEntity.ok(cartService.getCart(userId, pageable));
    }

    @PutMapping
    public ResponseEntity<?> updateCartItem(@AuthenticationPrincipal UUID userId,
                                            @Valid @RequestBody CartItemDTO cartItemDTO){
        return ResponseEntity.ok(cartService.updateCartItem(userId, cartItemDTO));
    }

    @PostMapping("delete")
    public ResponseEntity<?> deleteCartItem(@AuthenticationPrincipal UUID userId,
                                            @RequestBody List<UUID> cartItemIds) {
        return ResponseEntity.ok(cartService.deleteCartItem(userId, cartItemIds));
    }
}

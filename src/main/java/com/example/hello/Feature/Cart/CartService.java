package com.example.hello.Feature.Cart;

import com.example.hello.DataProjection.*;
import com.example.hello.Entity.Cart;
import com.example.hello.Feature.Cart.CartDTO.CartDTO;
import com.example.hello.Feature.Cart.CartDTO.CartItemDTO;
import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Mapper.CartItemMapper;
import com.example.hello.Middleware.ListResponse;
import com.example.hello.Middleware.Response;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Repository.*;
import com.example.hello.SseEmitter.SseService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartService {
    CartRepository cartRepository;
    CartItemRepository cartItemRepository;
    VariantRepository variantRepository;
    UserRepository userRepository;
    CartItemMapper cartItemMapper;
    ProductDetailMapping productDetailMapping;
    SseService sseService;

    @Transactional
    public Response<Void> addCartItem(UUID userId, CartItemDTO cartItemDTO) {
        var user = userRepository.findById(userId).orElseThrow(
                ()-> new EntityNotFoundException(StringApplication.FIELD.USER +
                        StringApplication.FIELD.NOT_EXIST)
        );
        log.info("Found user successfully");
        var variant = variantRepository.findById(cartItemDTO.getVariantId()).orElseThrow(
                ()-> new EntityNotFoundException(StringApplication.FIELD.PRODUCT +
                        StringApplication.FIELD.NOT_EXIST)
        );
        log.info("Found variant successfully");
        if(cartItemDTO.getQuantity() > variant.getStock()){
            throw new ConflictException(StringApplication.FIELD.PRODUCT +
                    StringApplication.FIELD.MAXIMUM);
        }
        log.info("Quantity < Stock successfully");
        var cart = cartRepository.findByUser(user).orElseGet(
                ()-> {
                    var c = Cart.builder()
                            .user(user)
                            .build();
                    cartRepository.save(c);
                    return c;
                }
        );
        log.info("Found or generated cart successfully");
        var cartItem = cartItemRepository.findByCartAndVariant(cart, variant)
                .orElseGet(()-> {
                    log.info("Cart item generated successfully");
                    if(cartItemRepository.countByCart(cart) >= 50){
                        log.error("Cart item > 50 item");
                        throw new ConflictException(StringApplication.FIELD.CART +
                                StringApplication.FIELD.MAXIMUM);
                    }
                    var cartItemCurrent = cartItemMapper.toCartItem(cartItemDTO);
                    cartItemCurrent.setQuantity(0);
                    sseService.sendSse("cart", 1, List.of(userId));
                    log.info("Send sse cart successfully");
                    return cartItemCurrent;
                });
        cartItem.setOldPrice(variant.getPrice());
        cartItem.setQuantity(cartItem.getQuantity() + cartItemDTO.getQuantity());
        cartItem.setVariant(variant);
        cartItem.setCart(cart);
        log.info("Cart saved successfully");
        cartItemRepository.save(cartItem);
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }

    @Transactional(readOnly = true)
    public Response<ListResponse<CartDTO>> getCart(UUID userId, Pageable pageable) {
        var user = userRepository.findById(userId).orElseThrow(
                ()-> new EntityNotFoundException(StringApplication.FIELD.USER +
                        StringApplication.FIELD.NOT_EXIST)
        );
        var productIds = cartItemRepository.getProductIds(userId, pageable);
        log.info("Found list product successfully");
        var products = productIds.getContent()
                .stream()
                .collect(Collectors.groupingBy(ProductInfo::getProductId));
        log.info("Group by product id successfully");
        cartRepository.findByUser(user).orElseGet(
                ()-> {
                    log.info("Cart generated successfully");
                    var c = Cart.builder()
                            .user(user)
                            .build();
                    cartRepository.save(c);
                    return c;
                }
        );
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                new ListResponse<>(
                        productIds.hasNext(),
                        productDetailMapping.mappingProductsDetail(products)
                )
        );
    }

    @Transactional
    public Response<Void> updateCartItem(UUID userId, CartItemDTO cartItemDTO) {
        var cart = cartRepository.findByCartItemIdAndUserId(cartItemDTO.getCartItemId(), userId).orElseThrow(
                ()-> new EntityNotFoundException(StringApplication.FIELD.REQUEST + StringApplication.FIELD.INVALID)
        );
        var variant = variantRepository.findById(cartItemDTO.getVariantId()).orElseThrow(
                ()-> new EntityNotFoundException(StringApplication.FIELD.PRODUCT +
                        StringApplication.FIELD.NOT_EXIST)
        );
        if(cartItemDTO.getQuantity() > variant.getStock()){
            log.error("Quantity < Stock");
            throw new ConflictException(StringApplication.FIELD.PRODUCT +
                    StringApplication.FIELD.MAXIMUM);
        }
        var cartItem = cartItemRepository.findById(cartItemDTO.getCartItemId()).orElseThrow(
                ()-> new EntityNotFoundException(StringApplication.FIELD.PRODUCT +
                        StringApplication.FIELD.NOT_EXIST)
        );
        cartItemRepository.findByCartAndVariant(cart, variant).ifPresentOrElse(item -> {
            log.info("Cart item found");
            if(!item.getCartItemId().equals(cartItem.getCartItemId())) {
                log.info("When cart item exist");
                cartItemRepository.delete(cartItem);
                log.info("Cart item deleted successfully");
                item.setQuantity(item.getQuantity() + cartItemDTO.getQuantity());
            }
            else {
                log.info("When cart item not exist");
                item.setQuantity(cartItemDTO.getQuantity());
            }
            log.info("Cart item set quantity successfully");
            log.info("Cart item updated successfully");
            cartItemRepository.save(item);
        }, () -> {
            log.info("Cart item not found");
            cartItem.setVariant(variant);
            cartItem.setQuantity(cartItemDTO.getQuantity());
            cartItemRepository.save(cartItem);
            log.info("Cart item generated successfully");
        });
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }

    @Transactional
    public Response<Void> deleteCartItem(UUID userId, List<UUID> cartItemIds) {
        cartItemRepository.deleteByCart_User_UserIdAndCartItemIdIn(userId, cartItemIds);
        log.info("Cart items deleted successfully");
        sseService.sendSse("cart", -cartItemIds.size(), List.of(userId));
        log.info("Send sse cart successfully");
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null);
    }
}

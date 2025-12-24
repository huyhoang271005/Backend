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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Transactional
    public Response<Void> addCartItem(UUID userId, CartItemDTO cartItemDTO) {
        var user = userRepository.findById(userId).orElseThrow(
                ()-> new EntityNotFoundException(StringApplication.FIELD.USER +
                        StringApplication.FIELD.NOT_EXIST)
        );
        var variant = variantRepository.findById(cartItemDTO.getVariantId()).orElseThrow(
                ()-> new EntityNotFoundException(StringApplication.FIELD.PRODUCT +
                        StringApplication.FIELD.NOT_EXIST)
        );
        if(cartItemDTO.getQuantity() > variant.getStock()){
            throw new ConflictException(StringApplication.FIELD.PRODUCT +
                    StringApplication.FIELD.MAXIMUM);
        }
        var cart = cartRepository.findByUser(user).orElseGet(
                ()-> {
                    var c = Cart.builder()
                            .user(user)
                            .build();
                    cartRepository.save(c);
                    return c;
                }
        );
        var cartItem = cartItemRepository.findByCartAndVariant(cart, variant)
                .orElseGet(()-> {
                    if(cartItemRepository.countByCart(cart) >= 50){
                        throw new ConflictException(StringApplication.FIELD.CART +
                                StringApplication.FIELD.MAXIMUM);
                    }
                    var cartItemCurrent = cartItemMapper.toCartItem(cartItemDTO);
                    cartItemCurrent.setQuantity(0);
                    return cartItemCurrent;
                });
        cartItem.setOldPrice(variant.getPrice());
        cartItem.setQuantity(cartItem.getQuantity() + cartItemDTO.getQuantity());
        cartItem.setVariant(variant);
        cartItem.setCart(cart);
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
        var products = productIds.getContent()
                .stream()
                .collect(Collectors.groupingBy(ProductInfo::getProductId));
        cartRepository.findByUser(user).orElseGet(
                ()-> {
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
            throw new ConflictException(StringApplication.FIELD.PRODUCT +
                    StringApplication.FIELD.MAXIMUM);
        }
        var cartItem = cartItemRepository.findById(cartItemDTO.getCartItemId()).orElseThrow(
                ()-> new EntityNotFoundException(StringApplication.FIELD.PRODUCT +
                        StringApplication.FIELD.NOT_EXIST)
        );
        cartItemRepository.findByCartAndVariant(cart, variant).ifPresentOrElse(item -> {

            if(!item.getCartItemId().equals(cartItem.getCartItemId())) {
                cartItemRepository.delete(cartItem);
                item.setQuantity(item.getQuantity() + cartItemDTO.getQuantity());
            }
            else {
                item.setQuantity(cartItemDTO.getQuantity());
            }
            cartItemRepository.save(item);
        }, () -> {
            cartItem.setVariant(variant);
            cartItem.setQuantity(cartItemDTO.getQuantity());
            cartItemRepository.save(cartItem);
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
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null);
    }
}

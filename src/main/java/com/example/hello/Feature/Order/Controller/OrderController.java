package com.example.hello.Feature.Order.Controller;

import com.example.hello.Feature.Order.DTO.OrderDTO;
import com.example.hello.Feature.Order.Service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("auth/orders")
public class OrderController {
    OrderService orderService;
    UUID id = UUID.fromString("22fe0072-d200-4ec4-a90a-d6e56d782f08");

    @PostMapping
    public ResponseEntity<?> addOrder(@AuthenticationPrincipal UUID userId,
                                      @RequestBody OrderDTO orderDTO) {
        return ResponseEntity.ok(orderService.addOrder(id, orderDTO));
    }

    @GetMapping("{orderId}")
    public ResponseEntity<?> getOrder(@AuthenticationPrincipal UUID userId,
                                      @PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrder(id, orderId));
    }

    @PatchMapping("{orderId}")
    public ResponseEntity<?> cancelOrder(@AuthenticationPrincipal UUID userId,
                                         @PathVariable UUID orderId){
        return ResponseEntity.ok(orderService.cancelOrder(id, orderId));
    }

    @GetMapping
    public ResponseEntity<?> getOrders(@AuthenticationPrincipal UUID userId,
                                       Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrders(id, pageable));
    }
}

package com.example.hello.Feature.Order.Controller;

import com.example.hello.Enum.OrderStatus;
import com.example.hello.Feature.Order.dto.OrderDTO;
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
@RequestMapping("orders")
public class OrderController {
    OrderService orderService;

    @PostMapping
    public ResponseEntity<?> addOrder(@AuthenticationPrincipal UUID userId,
                                      @RequestBody OrderDTO orderDTO) {
        return ResponseEntity.ok(orderService.addOrder(userId, orderDTO));
    }

    @GetMapping("{orderId}")
    public ResponseEntity<?> getOrder(@AuthenticationPrincipal UUID userId,
                                      @PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrder(userId, orderId));
    }

    @GetMapping("/count")
    public ResponseEntity<?> getOrderCount(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(orderService.getCountOrders(userId));
    }

    @PatchMapping("{orderId}")
    public ResponseEntity<?> updateStatusOrder(@AuthenticationPrincipal UUID userId,
                                               @PathVariable UUID orderId,
                                               @RequestBody OrderStatus orderStatus){
        return ResponseEntity.ok(orderService.updateOrder(userId, orderId, orderStatus));
    }

    @GetMapping
    public ResponseEntity<?> getOrders(@AuthenticationPrincipal UUID userId,
                                       @RequestParam(required = false) String searchInput,
                                       Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrders(userId, searchInput, pageable));
    }
}

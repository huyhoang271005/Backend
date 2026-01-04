package com.example.hello.Feature.Order.Controller;

import com.example.hello.Enum.OrderStatus;
import com.example.hello.Feature.Order.Service.OrderAdminService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("auth/admin/orders")
public class OrderAdminController {
    OrderAdminService orderAdminService;

//    @PreAuthorize("hasAuthority('GET_ORDERS_BY_STATUS')")
    @GetMapping
    public ResponseEntity<?> getOrderAdmin(@RequestParam(required = false) OrderStatus orderStatus,
                                           @RequestParam(required = false) UUID orderId,
                                           Pageable pageable){
        return ResponseEntity.ok(orderAdminService.getOrdersAdmin(orderStatus, orderId, pageable));
    }

//    @PreAuthorize("hasAuthority('CONFIRM_ORDER')")
    @PatchMapping("{orderId}")
    public ResponseEntity<?> updateOrderStatus(@PathVariable UUID orderId,
                                               @RequestBody OrderStatus orderStatus){
        return ResponseEntity.ok(orderAdminService.updateOrderStatus(orderId, orderStatus));
    }
}

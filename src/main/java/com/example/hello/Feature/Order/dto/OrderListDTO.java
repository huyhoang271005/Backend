package com.example.hello.Feature.Order.dto;

import com.example.hello.Enum.OrderStatus;
import com.example.hello.Enum.PaymentMethod;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderListDTO {
    UUID orderId;
    OrderStatus orderStatus;
    Instant createdAt;
    Instant updatedAt;
    PaymentMethod paymentMethod;
    List<OrderItemDTO> orderItemDTOList;
}

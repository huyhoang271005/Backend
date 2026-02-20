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
public class OrderListAdminDTO {
    UUID orderId;
    OrderStatus orderStatus;
    String contactName;
    String phone;
    String address;
    PaymentMethod paymentMethod;
    Instant createdAt;
    Instant paymentAt;
    List<OrderItemDTO> orderItemDTOList;
}

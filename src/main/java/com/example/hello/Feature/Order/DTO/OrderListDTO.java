package com.example.hello.Feature.Order.DTO;

import com.example.hello.Enum.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
    List<OrderItemDTO> orderItemDTOList;
}

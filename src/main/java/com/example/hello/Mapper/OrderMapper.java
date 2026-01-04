package com.example.hello.Mapper;

import com.example.hello.Feature.Authentication.DataProjection.OrderInfo;
import com.example.hello.Entity.Contact;
import com.example.hello.Entity.Order;
import com.example.hello.Entity.OrderItem;
import com.example.hello.Feature.Order.DTO.OrderDTO;
import com.example.hello.Feature.Order.DTO.OrderItemDTO;
import com.example.hello.Feature.Order.DTO.OrderListAdminDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    Order toOrder(Contact contact);
    OrderItem toOrderItem(OrderItemDTO orderItemDTO);
    OrderDTO toOrderDTO(Order order);
    OrderListAdminDTO toOrderListAdminDTO(Order order);
    OrderItemDTO toOrderItemDTO(OrderInfo orderInfo);
}

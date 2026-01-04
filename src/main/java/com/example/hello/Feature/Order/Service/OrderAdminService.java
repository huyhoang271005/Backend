package com.example.hello.Feature.Order.Service;

import com.example.hello.Feature.Authentication.DataProjection.AttributeValueByVariantId;
import com.example.hello.Feature.Authentication.DataProjection.OrderInfo;
import com.example.hello.Enum.OrderStatus;
import com.example.hello.Feature.Order.DTO.OrderListAdminDTO;
import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Infrastructure.Exception.UnprocessableEntityException;
import com.example.hello.Mapper.OrderMapper;
import com.example.hello.Middleware.ListResponse;
import com.example.hello.Middleware.Response;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderAdminService {
    OrderRepository orderRepository;
    VariantValueRepository variantValueRepository;
    OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public Response<ListResponse<OrderListAdminDTO>> getOrdersAdmin(OrderStatus orderStatus, UUID orderId, Pageable pageable) {
        var orderItems = orderRepository.getOrdersAdminInfo(pageable, orderStatus, orderId);
        log.info("Found orders admin successfully");
        var variantValues = variantValueRepository.getAttributeValuesVariantIdIn(orderItems
                .stream()
                .map(OrderInfo::getVariantId)
                .distinct()
                .toList())
                .stream()
                .collect(Collectors.groupingBy(AttributeValueByVariantId::getVariantId));
        log.info("Found variant values successfully");
        var orders = orderItems.getContent().stream()
                .collect(Collectors.groupingBy(orderInfo -> orderInfo.getOrder().getOrderId()));
        var orderListAdmin = orders.keySet()
                .stream()
                .map(uuid -> {
                    var order = orders.get(uuid);
                    var orderAdmin = orderMapper.toOrderListAdminDTO(order.getFirst().getOrder());
                    orderAdmin.setOrderItemDTOList(order.stream()
                            .map(orderInfo -> {
                                var orderItemDTO = orderMapper.toOrderItemDTO(orderInfo);
                                orderItemDTO.setAttributeValues(variantValues.get(orderInfo.getVariantId())
                                        .stream()
                                        .map(AttributeValueByVariantId::getAttributeName)
                                        .toList());
                                return orderItemDTO;
                            })
                            .toList());
                    return orderAdmin;
                })
                .toList();
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                new ListResponse<>(
                        orderItems.hasNext(),
                        orderListAdmin
                )
        );
    }

    @Transactional
    public Response<Void> updateOrderStatus(UUID orderId, OrderStatus orderStatus) {
        var order = orderRepository.findById(orderId).orElseThrow(
                () -> new EntityNotFoundException(StringApplication.FIELD.ORDER +
                        StringApplication.FIELD.NOT_EXIST)
        );
        log.info("Found order admin successfully");
        if(orderStatus == OrderStatus.CONFIRMED) {
            if(order.getOrderStatus() == OrderStatus.PENDING){
                order.setOrderStatus(OrderStatus.DELIVERING);
                log.info("Order {} was set status is delivering", orderId);
            }
            else if(order.getOrderStatus() == OrderStatus.DELIVERING){
                order.setOrderStatus(OrderStatus.DELIVERED);
                log.info("Order {} was set status is delivered", orderId);
            }
            else {
                log.error("Order cant change status on {}", order.getOrderStatus());
                throw new ConflictException(StringApplication.FIELD.REQUEST +
                        StringApplication.FIELD.INVALID);
            }
        }
        else if(orderStatus == OrderStatus.CANCELED) {
            if(order.getOrderStatus() == OrderStatus.PENDING || order.getOrderStatus() == OrderStatus.DELIVERING) {
                order.setOrderStatus(OrderStatus.CANCELED);
                log.info("Order {} was canceled", orderId);
            }
        }
        else {
            log.error("Order status not confirmed");
            throw new UnprocessableEntityException(StringApplication.FIELD.REQUEST +
                    StringApplication.FIELD.INVALID);
        }
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }
}

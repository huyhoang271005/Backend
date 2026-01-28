package com.example.hello.Feature.Order.Service;

import com.example.hello.DataProjection.AttributeValueByVariantId;
import com.example.hello.DataProjection.OrderInfo;
import com.example.hello.DataProjection.OrderItemInfo;
import com.example.hello.DataProjection.VariantInfo;
import com.example.hello.Entity.Product;
import com.example.hello.Entity.Variant;
import com.example.hello.Enum.OrderStatus;
import com.example.hello.Enum.PaymentMethod;
import com.example.hello.Feature.Order.DTO.OrderDTO;
import com.example.hello.Feature.Order.DTO.OrderItemDTO;
import com.example.hello.Feature.Order.DTO.OrderListDTO;
import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Infrastructure.Exception.UnprocessableEntityException;
import com.example.hello.Mapper.OrderMapper;
import com.example.hello.Middleware.ListResponse;
import com.example.hello.Middleware.Response;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Repository.*;
import com.example.hello.SseEmitter.SseService;
import com.example.hello.SseEmitter.SseTopicName;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService {
    OrderRepository orderRepository;
    ContactRepository contactRepository;
    UserRepository userRepository;
    VariantRepository variantRepository;
    CartItemRepository cartItemRepository;
    VariantValueRepository variantValueRepository;
    OrderMapper orderMapper;
    SseService sseService;
    ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    @Async
    public void countTotalSalesByProductIds(List<UUID> variantIds) {
        var productIds = productRepository.findByVariantIds(variantIds);
        var products = productRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));
        var variants = variantRepository.findVariantInfoByProductIds(productIds)
                .stream()
                .collect(Collectors.groupingBy(VariantInfo::getProductId));
        productIds.forEach(productId -> {
           var sold = variants.get(productId)
                   .stream()
                   .mapToInt(VariantInfo::getSold)
                   .sum();
           products.get(productId).setTotalSales(sold);

        });
        productRepository.saveAll(products.values());
        log.info("Updated total sales by product ids: {}", productIds);
    }
    @Async
    public void updateProductWhenCancel(List<UUID> orderIds) {
        var variantInfos = orderItemRepository.getOrderItemsVariant(orderIds);
        var variantInfosGroup = variantInfos.stream()
                .collect(Collectors
                        .toMap(OrderItemInfo::getVariantId, Function.identity()));
        var variantIds = variantInfos.stream()
                .map(OrderItemInfo::getVariantId)
                .distinct()
                .toList();
        var variants = variantRepository.findAllById(variantIds);
        variants.forEach(variant -> {
            variant.setSold(variant.getSold() -
                    variantInfosGroup.get(variant.getVariantId()).getQuantity());
            variant.setStock(variant.getStock() +
                    variantInfosGroup.get(variant.getVariantId()).getQuantity());
        });
        variantRepository.saveAll(variants);
        log.info("Updated variant when cancel successfully");
        countTotalSalesByProductIds(variantIds);
    }
    @Transactional
    public Response<Map<String, UUID>> addOrder(UUID userId, OrderDTO  orderDTO) {
        var user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(StringApplication.FIELD.USER +
                        StringApplication.FIELD.NOT_EXIST)
        );
        var contact = contactRepository.findById(orderDTO.getContactId()).orElseThrow(
                () -> new EntityNotFoundException(StringApplication.FIELD.CONTACT +
                        StringApplication.FIELD.NOT_EXIST)
        );
        if(orderDTO.getOrderItemDTOList().isEmpty()){
            log.info("Order item is empty");
            throw new ConflictException(StringApplication.FIELD.REQUEST +
                    StringApplication.FIELD.INVALID);
        }
        var variants = variantRepository.findAllById(orderDTO.getOrderItemDTOList()
                .stream()
                .map(OrderItemDTO::getVariantId)
                .toList())
                .stream()
                .collect(Collectors.toMap(Variant::getVariantId, Function.identity()));
        log.info("Variant found successfully");
        if(variants.size() != orderDTO.getOrderItemDTOList().size()) {
            log.error("Variant size in db different variant size client ");
            throw new UnprocessableEntityException(StringApplication.FIELD.PRODUCT +
                    StringApplication.FIELD.NOT_EXIST);
        }
        var order = orderMapper.toOrder(contact);
        order.setPaymentMethod(orderDTO.getPaymentMethod());
        if(orderDTO.getPaymentMethod() == PaymentMethod.COD){
            order.setOrderStatus(OrderStatus.PENDING);
        }
        else {
            order.setOrderStatus(OrderStatus.WAITING);
        }
        log.info("Order generated");
        order.setUser(user);
        var orderItem = orderDTO.getOrderItemDTOList().stream()
                .map(orderItemDTO -> {
                    if(orderItemDTO.getQuantity() > variants.get(orderItemDTO.getVariantId()).getStock()){
                        log.error("Order quantity exceeds stock");
                        throw new ConflictException("Order quantity exceeds stock");
                    }
                    var orderItemCurrent = orderMapper.toOrderItem(orderItemDTO);
                    var variant = variants.get(orderItemDTO.getVariantId());
                    orderItemCurrent.setPrice(variant.getPrice());
                    orderItemCurrent.setOriginalPrice(variant.getOriginalPrice());
                    orderItemCurrent.setVariant(variant);
                    orderItemCurrent.setOrder(order);
                    variant.setStock(variant.getStock() - orderItemDTO.getQuantity());
                    variant.setSold(variant.getSold() + orderItemDTO.getQuantity());
                    return orderItemCurrent;
                })
                .toList();
        order.setOrderItems(orderItem);
        orderRepository.save(order);
        log.info("Order item generated successfully");
        var cartItemIds = new ArrayList<UUID>();
        orderDTO.getOrderItemDTOList().forEach(orderItemDTO -> {
            if(orderItemDTO.getVariantId() != null) {
                cartItemIds.add(orderItemDTO.getCartItemId());
            }
        });
        if(!cartItemIds.isEmpty()) {
            log.info("Cart item not null");
            cartItemRepository.deleteByCartItemIdIn(cartItemIds);
            log.info("Deleted cart items successfully");
            sseService.sendSse(SseTopicName.cart.name(), -cartItemIds.size(), List.of(userId));
            log.info("Cart sse sent successfully");
        }
        //Update totalSales
        countTotalSalesByProductIds(orderDTO.getOrderItemDTOList().stream()
                .map(OrderItemDTO::getVariantId)
                .toList());
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                Map.of("orderId", order.getOrderId())
        );
    }

    @Transactional(readOnly = true)
    public Response<OrderDTO> getOrder(UUID userId, UUID orderId) {
        var orders = orderRepository.getOrderInfo(userId, orderId)
                .stream()
                .collect(Collectors.groupingBy(orderInfo ->  orderInfo.getOrder().getOrderId()));
        log.info("Orders found successfully");
        var order = orders.get(orderId).getFirst();
        var attributeValue = variantValueRepository.getAttributeValuesVariantIdIn(orders.get(orderId)
                        .stream()
                        .map(OrderInfo::getVariantId)
                        .toList())
                .stream()
                .collect(Collectors.groupingBy(AttributeValueByVariantId::getVariantId));
        log.info("Attribute values found successfully");
        var orderDTO = orderMapper.toOrderDTO(order.getOrder());
        orderDTO.setOrderItemDTOList(orders.get(orderId)
                .stream()
                .map(orderInfo -> {
                    var orderItemDTO = orderMapper.toOrderItemDTO(orderInfo);
                    orderItemDTO.setAttributeValues(attributeValue.get(orderInfo.getVariantId())
                            .stream()
                            .map(AttributeValueByVariantId::getAttributeValueName)
                            .toList());
                    return orderItemDTO;
                })
                .toList());
        log.info("Order mapping successfully");
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                orderDTO
        );
    }

    private List<OrderListDTO> getOrderList(Page<OrderInfo> orderItems) {
        var orders = orderItems.getContent()
                .stream()
                .collect(Collectors.groupingBy(orderInfo -> orderInfo.getOrder().getOrderId()));
        var variantValues = variantValueRepository.getAttributeValuesVariantIdIn(orderItems.getContent()
                        .stream()
                        .map(OrderInfo::getVariantId)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.groupingBy(AttributeValueByVariantId::getVariantId));
        log.info("Variant values found successfully");
        log.info("Orders list mapping successfully");
        return orders.keySet()
                .stream()
                .map(uuid -> {
                    var order = orders.get(uuid);
                    return OrderListDTO.builder()
                            .orderId(uuid)
                            .orderStatus(order.getFirst().getOrder().getOrderStatus())
                            .updatedAt(order.getFirst().getUpdatedAt())
                            .orderItemDTOList(order
                                    .stream()
                                    .map(orderInfo -> {
                                        var orderItemDTO = orderMapper.toOrderItemDTO(orderInfo);
                                        orderItemDTO.setAttributeValues(variantValues.get(orderInfo.getVariantId())
                                                .stream()
                                                .map(AttributeValueByVariantId::getAttributeName)
                                                .toList());
                                        return orderItemDTO;
                                    })
                                    .toList())
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Response<ListResponse<OrderListDTO>> getOrders(UUID userId, Pageable pageable) {
        var orderItems = orderRepository.getOrdersInfo(userId, pageable);
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                new ListResponse<>(
                        orderItems.hasNext(),
                        getOrderList(orderItems)
                )
        );
    }

    @Transactional
    public Response<Void> updateOrder(UUID userId, UUID orderId, OrderStatus orderStatus) {
        var order = orderRepository.findByOrderIdAndUser_UserId((orderId), userId)
                .orElseThrow(() -> new EntityNotFoundException(StringApplication.FIELD.ORDER +
                        StringApplication.FIELD.NOT_EXIST));
        log.info("Order found successfully");
        if(orderStatus == OrderStatus.CANCELED) {
            if(order.getOrderStatus() == OrderStatus.WAITING){
                log.info("Order {} was set status cancelled", orderId);
                order.setOrderStatus(OrderStatus.CANCELED);
                updateProductWhenCancel(List.of(orderId));
            }
            else {
                log.error("Order {} cant cancelled with status {}", orderId, order.getOrderStatus());
                throw new ConflictException(StringApplication.FIELD.CANT_CANCEL);
            }
        }
        else if(orderStatus == OrderStatus.COMPLETED) {
            if(order.getOrderStatus() == OrderStatus.DELIVERED) {
                log.info("Order {} was set status {}", orderId, OrderStatus.COMPLETED);
                order.setOrderStatus(OrderStatus.COMPLETED);
            }
            else {
                log.error("Order {} cant set with status {}", orderId, order.getOrderStatus());
                throw new ConflictException(StringApplication.FIELD.REQUEST +
                        StringApplication.FIELD.INVALID);
            }
        }
        else {
            log.error("Request order status invalid with {}", orderStatus);
            throw new ConflictException(StringApplication.FIELD.REQUEST +
                    StringApplication.FIELD.INVALID);
        }
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }
}

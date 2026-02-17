package com.example.hello.Feature.Feedback.Service;

import com.example.hello.Feature.ProductsManager.dto.AttributeValueByVariantId;
import com.example.hello.Feature.Feedback.dto.FeedbackReplyInfo;
import com.example.hello.Feature.Order.dto.OrderItemInfo;
import com.example.hello.Entity.FeedbackOrderItem;
import com.example.hello.Enum.OrderStatus;
import com.example.hello.Feature.Feedback.dto.FeedbackCandidatesDTO;
import com.example.hello.Feature.Feedback.dto.FeedbackRequest;
import com.example.hello.Feature.Feedback.dto.FeedbackResponse;
import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Infrastructure.Exception.UnprocessableEntityException;
import com.example.hello.Mapper.FeedbackMapper;
import com.example.hello.Mapper.FeedbackReplyMapper;
import com.example.hello.Middleware.ListResponse;
import com.example.hello.Middleware.Response;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Feature.Order.Repository.OrderItemRepository;
import com.example.hello.Feature.ProductsManager.Repository.VariantValueRepository;
import com.example.hello.Feature.Feedback.Repository.FeedbackOrderItemRepository;
import com.example.hello.Feature.ProductsManager.Repository.ProductRepository;
import com.example.hello.Feature.Feedback.Repository.FeedbackRepository;
import com.example.hello.Feature.Feedback.Repository.FeedbackReplyRepository;
import com.example.hello.Feature.Order.Repository.OrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedbackService {
    OrderItemRepository orderItemRepository;
    VariantValueRepository variantValueRepository;
    FeedbackOrderItemRepository feedbackOrderItemRepository;
    ProductRepository productRepository;
    FeedbackMapper feedbackMapper;
    FeedbackRepository feedbackRepository;
    FeedbackReplyRepository feedbackReplyRepository;
    OrderRepository orderRepository;
    FeedbackReplyMapper feedbackReplyMapper;

    @Transactional(readOnly = true)
    public Response<List<FeedbackCandidatesDTO>> getFeedbackCandidates(UUID orderId){
        var orderItem = orderItemRepository.getOrderItemsFeedback(orderId);
        log.info("Found order item can feedback candidates");
        if(orderItem.isEmpty()){
            return new Response<>(
                    true,
                    StringApplication.FIELD.SUCCESS,
                    new ArrayList<>()
            );
        }
        if(orderItem.getFirst().getOrderStatus() != OrderStatus.COMPLETED){
            log.error("Order status is {} not COMPLETED", orderItem.getFirst().getOrderStatus());
            throw new UnprocessableEntityException("Order status is not COMPLETED");
        }
        var orderItemGroup = orderItem
                .stream()
                .collect(Collectors.groupingBy(OrderItemInfo::getProductId));
        var variantValues = variantValueRepository.getAttributeValuesVariantIdIn(orderItem
                .stream()
                .map(OrderItemInfo::getVariantId)
                .distinct()
                .toList())
                .stream()
                .collect(Collectors.groupingBy(AttributeValueByVariantId::getVariantId));
        log.info("Found variant values by variant id can feedback candidates");
        var listFeedbackResponse = orderItemGroup.entrySet()
                .stream()
                .map(uuidListEntry -> {
                    var variantValuesInProduct = uuidListEntry.getValue().stream()
                            .map(OrderItemInfo::getVariantId)
                            .distinct()
                            .map(uuid -> variantValues.get(uuid)
                                        .stream()
                                        .collect(Collectors.toMap(AttributeValueByVariantId::getAttributeName,
                                                AttributeValueByVariantId::getAttributeValueName))
                            )
                            .toList();
                    log.info("Mapping variantValues to Hash Map successfully");
                    return  FeedbackCandidatesDTO.builder()
                            .productId(uuidListEntry.getKey())
                            .productName(uuidListEntry.getValue().getFirst().getProductName())
                            .imageUrl(uuidListEntry.getValue().getFirst().getImageUrl())
                            .orderItemIds(uuidListEntry.getValue().stream()
                                    .map(OrderItemInfo::getOrderItemId)
                                    .toList())
                            .variants(variantValuesInProduct)
                            .build();

                })
                .toList();
        log.info("Mapping feedback candidates successfully");
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                listFeedbackResponse
        );
    }

    @Transactional
    public Response<Void> addFeedback(UUID userId, FeedbackRequest feedbackRequest){
        var orderItemIds = feedbackRequest.getOrderItemIds();
        var valid = feedbackOrderItemRepository.validateOrderItemFeedback(orderItemIds)
                .orElseThrow(()-> new ConflictException("Order Item Feedback Validation Failed"));
        Long totalItems = valid.getTotalItems();
        log.info("total items is {}", totalItems);
        Long orderCount = valid.getOrderCount();
        log.info("order count is {}", orderCount);
        Long feedbackCount = valid.getFeedbackCount();
        log.info("feedback count is {}", feedbackCount);

        if(totalItems != orderItemIds.size()){
            log.error("Order items in db not match order item id list");
            throw new UnprocessableEntityException(StringApplication.FIELD.REQUEST +
                    StringApplication.FIELD.INVALID);
        }
        if(orderCount != 1){
            log.error("Order item id list not in one order");
            throw new ConflictException(StringApplication.FIELD.REQUEST +
                    StringApplication.FIELD.INVALID);
        }
        if(feedbackCount > 0){
            log.error("Some order item was feedback");
            throw new ConflictException(StringApplication.FIELD.REQUEST +
                    StringApplication.FIELD.INVALID);
        }
        var product = productRepository.findByOrderItemId(orderItemIds.getFirst())
                .orElseThrow(
                        ()-> new EntityNotFoundException(StringApplication.FIELD.PRODUCT +
                                StringApplication.FIELD.NOT_EXIST)
                );
        Double ratingAvg = product.getRatingAvg();
        Integer ratingCount = product.getRatingCount();
        product.setRatingAvg((ratingAvg * ratingCount + feedbackRequest.getRating())/
                (ratingCount + 1));
        product.setRatingCount(ratingCount + 1);
        var orderWithUserId = orderRepository.findOrderAndUserIdByOrderItemId(orderItemIds.getFirst())
                .orElseThrow(
                        ()-> new EntityNotFoundException(StringApplication.FIELD.ORDER +
                                StringApplication.FIELD.NOT_EXIST)
                );
        if(!orderWithUserId.getUserId().equals(userId)){
            log.error("Order not owned by user {}", userId);
            throw new ConflictException(StringApplication.FIELD.USER +
                    StringApplication.FIELD.INVALID);
        }
        var order = orderWithUserId.getOrder();
        if(order.getOrderStatus() != OrderStatus.COMPLETED){
            log.error("Order {} status is {} not COMPLETED", order.getOrderId(), order.getOrderStatus());
            throw new ConflictException(StringApplication.FIELD.REQUEST +
                    StringApplication.FIELD.INVALID);
        }
        var feedback = feedbackMapper.toFeedback(feedbackRequest);
        feedback.setOrder(order);
        feedback.setProduct(product);
        feedbackRepository.save(feedback);
        log.info("Added feedback successfully");
        var feedbackOrderItems = orderItemRepository.findAllById(orderItemIds)
                .stream()
                .map(orderItem -> FeedbackOrderItem.builder()
                        .orderItem(orderItem)
                        .feedback(feedback)
                        .build())
                .toList();
        feedbackOrderItemRepository.saveAll(feedbackOrderItems);
        log.info("Added feedback order items successfully");
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }

    @Transactional(readOnly = true)
    public Response<ListResponse<FeedbackResponse>> getFeedbacks(UUID productId, Pageable pageable){
        var feedbacksPage = feedbackRepository.getFeedbackInfo(productId, pageable);
        log.info("Found feedbacks successfully");
        var feedbacks = feedbacksPage.getContent()
                .stream()
                .map(feedbackMapper::toFeedbackResponse)
                .toList();
        var feedbackReply = feedbackReplyRepository
                .getFeedbackRepliesByFeedbackIds(feedbacks.stream()
                        .map(FeedbackResponse::getFeedbackId)
                        .toList())
                .stream()
                .collect(Collectors.toMap(FeedbackReplyInfo::getFeedbackId, Function.identity()));
        log.info("Found feedback replies successfully");
        feedbacks.forEach(feedbackResponse ->
                feedbackResponse.setReply(feedbackReplyMapper
                .toFeedbackReplyDTO(feedbackReply.get(feedbackResponse.getFeedbackId()))));
        log.info("Mapping feedbacks and reply successfully");
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                new ListResponse<>(
                        feedbacksPage.hasNext(),
                        feedbacks
                )
        );
    }
}

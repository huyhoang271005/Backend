package com.example.hello.Feature.Order.Scheduled;

import com.example.hello.Enum.OrderStatus;
import com.example.hello.Feature.Notification.NotificationDTO;
import com.example.hello.Feature.Notification.NotificationService;
import com.example.hello.Infrastructure.Security.CorsConfig;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Repository.OrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderScheduled {
    OrderRepository orderRepository;
    NotificationService notificationService;

    @Scheduled(fixedRate = 5*60*1000)
    @Transactional
    public void cancelOrder() {
        Instant timeAgo = Instant.now().minus(1, ChronoUnit.DAYS);
        orderRepository.updateOrderStatus(OrderStatus.CANCELED, timeAgo, Instant.now(), OrderStatus.WAITING);
        var users = orderRepository.findUserOrderByTimeAgo(timeAgo, OrderStatus.WAITING);
        log.info("Foud orders need cancel scheduled successfully");
        if(!users.isEmpty()){
            log.info("Send notification canceled order scheduled successfully");
            notificationService.sendNotification(users,
                    NotificationDTO.builder()
                            .title(StringApplication.FIELD.ORDER)
                            .message(StringApplication.FIELD.ORDER +
                                    StringApplication.NOTIFICATION.CANCELED_BY_SYSTEM)
                            .linkUrl(CorsConfig.BASE_URL + "/orders")
                            .build());
        }
    }

    @Scheduled(fixedRate = 3*60*1000)
    @Transactional
    public void updateStatusOrder() {
        Instant timeAgo = Instant.now().minus(15, ChronoUnit.MINUTES);
        orderRepository.updateOrderStatus(OrderStatus.WAITING, timeAgo, Instant.now(), OrderStatus.PAYING);
        log.info("Update order status was paying to waiting successfully");
    }
}

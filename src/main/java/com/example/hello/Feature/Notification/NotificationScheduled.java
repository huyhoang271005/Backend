package com.example.hello.Feature.Notification;

import com.example.hello.Repository.NotificationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationScheduled {
    NotificationRepository notificationRepository;

    @Scheduled(fixedRate = 60*60*1000)
    @Transactional
    public void deleteNotifications() {
        notificationRepository.deleteNotifications();
        log.info("Deleted orphan notifications scheduled successfully");
    }
}

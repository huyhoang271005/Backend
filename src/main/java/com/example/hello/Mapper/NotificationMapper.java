package com.example.hello.Mapper;

import com.example.hello.DataProjection.NotificationInfo;
import com.example.hello.Entity.Notification;
import com.example.hello.Feature.Notification.NotificationDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationDTO toNotificationDTO(Notification notification);
    NotificationDTO toNotificationDTO(NotificationInfo notificationInfo);
    Notification toNotification(NotificationDTO notificationDTO);
}

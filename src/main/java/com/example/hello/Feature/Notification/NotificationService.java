package com.example.hello.Feature.Notification;

import com.example.hello.Entity.User;
import com.example.hello.Entity.UserNotification;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Mapper.NotificationMapper;
import com.example.hello.Middleware.ListResponse;
import com.example.hello.Middleware.Response;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Repository.NotificationRepository;
import com.example.hello.Repository.UserNotificationRepository;
import com.example.hello.Repository.UserRepository;
import com.example.hello.SseEmitter.SseService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {
    NotificationRepository notificationRepository;
    UserNotificationRepository userNotificationRepository;
    UserRepository userRepository;
    NotificationMapper notificationMapper;
    SseService sseService;

    @Transactional
    public void sendNotification(List<User> userList, NotificationDTO notificationDTO) {
        var notification = notificationMapper.toNotification(notificationDTO);
        notificationRepository.save(notification);
        var userNotifications = userList.stream()
                .map(user ->  UserNotification.builder()
                        .notification(notification)
                        .user(user)
                        .isRead(false)
                        .build())
                .toList();
        userNotificationRepository.saveAll(userNotifications);
        String topicName = "notifications";
        sseService.sendNotification(topicName, notificationDTO, userList.stream()
                .map(User::getUserId)
                .toList());
    }

    @Transactional
    public Response<Void> sendAll(NotificationDTO notificationDTO) {
        var listUser = userRepository.findAll();
        sendNotification(listUser, notificationDTO);
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }

    @Transactional(readOnly = true)
    public Response<ListResponse<NotificationDTO>> getNotifications(UUID userId, Pageable pageable) {
        var notifications = userNotificationRepository.getNotificationsByUserId(userId, pageable);
        var notificationDTOs = notifications.getContent().stream()
                .map(notificationMapper::toNotificationDTO)
                .toList();
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                new ListResponse<>(
                        notifications.hasNext(),
                        notificationDTOs
                )
        );
    }

    @Transactional
    public Response<Void> sendNotificationToRole(UUID roleId, NotificationDTO notificationDTO) {
        var user = userRepository.findByRole_RoleId(roleId);
        sendNotification(user, notificationDTO);
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }

    @Transactional
    public Response<Void> sendNotificationToUser(UUID userId, NotificationDTO notificationDTO) {
        var user =  userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(StringApplication.FIELD.USER +
                        StringApplication.FIELD.NOT_EXIST)
        );
        sendNotification(List.of(user), notificationDTO);
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }

    @Transactional
    public Response<Void> deleteNotification(UUID userId, List<UUID> userNotificationIds) {
        userNotificationRepository.deleteByUser_UserIdAndUserNotificationIdIn(userId, userNotificationIds);
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }

    @Transactional
    public Response<Void> updateNotification(UUID userId, List<UUID> userNotificationIds) {
        userNotificationRepository.updateUserNotifications(userId, userNotificationIds);
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                null
        );
    }

}

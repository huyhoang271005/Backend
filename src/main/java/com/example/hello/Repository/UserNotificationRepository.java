package com.example.hello.Repository;

import com.example.hello.DataProjection.NotificationInfo;
import com.example.hello.Entity.UserNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface UserNotificationRepository extends JpaRepository<UserNotification, UUID> {
    @Query("""
            select un.userNotificationId as userNotificationId, un.isRead as isRead,
                    n.title as title, n.message as message, n.linkUrl as linkUrl,
                     n.createdAt as createdAt
            from UserNotification un
            join un.notification n
            where un.user.userId = :userId
            order by n.createdAt desc
            """)
    Page<NotificationInfo> getNotificationsByUserId(UUID userId, Pageable pageable);

    void deleteByUser_UserIdAndUserNotificationIdIn(UUID userUserId, List<UUID> userNotificationIds);

    @Modifying
    @Query("""
            update UserNotification un
            set un.isRead = true
            where un.user.userId = :userId and un.userNotificationId in :userNotificationIds
            """)
    void updateUserNotifications(UUID userId, List<UUID> userNotificationIds);

    Integer countByUser_UserIdAndIsRead(UUID userId, Boolean isRead);
}
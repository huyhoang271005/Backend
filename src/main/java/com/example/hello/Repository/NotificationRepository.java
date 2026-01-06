package com.example.hello.Repository;

import com.example.hello.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    @Modifying
    @Query("""
            delete from Notification n
            where not exists (
                        select un
                        from UserNotification un
                        where un.notification = n
                        )
            """)
    void deleteNotificationsOrphan();
}
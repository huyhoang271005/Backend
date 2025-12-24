package com.example.hello.Feature.Notification;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("auth/notifications")
public class NotificationController {
    NotificationService notificationService;
    UUID id = UUID.fromString("8e9bad10-ef93-4d4c-92d7-fd096b21fc94");
    @PostMapping
    public ResponseEntity<?> sendNotificationAll(@RequestBody NotificationDTO notificationDTO) {
        return ResponseEntity.ok(notificationService.sendAll(notificationDTO));
    }

    @PostMapping("{userId}")
    public ResponseEntity<?> sendNotification(@PathVariable String userId, @RequestBody NotificationDTO notificationDTO) {
        return ResponseEntity.ok(notificationService.sendNotificationToUser(id, notificationDTO));
    }

    @PostMapping("roles/{roleId}")
    public ResponseEntity<?> sendNotificationToRole(@PathVariable UUID roleId,
                                              @RequestBody NotificationDTO notificationDTO) {
        return ResponseEntity.ok(notificationService.sendNotificationToRole(roleId, notificationDTO));
    }

    @GetMapping
    public ResponseEntity<?> getNotifications(@AuthenticationPrincipal UUID  userId, Pageable pageable) {
        return ResponseEntity.ok(notificationService.getNotifications(id, pageable));
    }

    @PatchMapping
    public ResponseEntity<?> readNotifications(@AuthenticationPrincipal UUID  userId,
                                               @RequestBody List<UUID> userNotificationIds) {
        return ResponseEntity.ok(notificationService.updateNotification(id, userNotificationIds));
    }

    @PostMapping("delete")
    public ResponseEntity<?> deleteNotifications(@AuthenticationPrincipal UUID  userId,
                                                 @RequestBody List<UUID> userNotificationIds) {
        return ResponseEntity.ok(notificationService.deleteNotification(id, userNotificationIds));
    }
}

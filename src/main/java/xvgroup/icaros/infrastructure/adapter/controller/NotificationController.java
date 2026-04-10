package xvgroup.icaros.infrastructure.adapter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import xvgroup.icaros.application.dto.NotificationResponse;
import xvgroup.icaros.domain.service.NotificationService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // GET /notifications - list all my notifications
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(JwtAuthenticationToken token) {
        List<NotificationResponse> notifications = notificationService.getMyNotifications(token);
        return ResponseEntity.ok(notifications);
    }

    // GET /notifications/unread/count - count unread notifications
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> countUnread(JwtAuthenticationToken token) {
        long count = notificationService.countUnread(token);
        return ResponseEntity.ok(Map.of("unread", count));
    }

    // PATCH /notifications/read-all - mark all notifications as read
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(JwtAuthenticationToken token) {
        notificationService.markAllAsRead(token);
        return ResponseEntity.noContent().build();
    }

    // PATCH /notifications/{id}/read - mark a specific notification as read
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id, JwtAuthenticationToken token) {
        notificationService.markAsRead(id, token);
        return ResponseEntity.noContent().build();
    }
}

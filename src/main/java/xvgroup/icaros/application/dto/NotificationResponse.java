package xvgroup.icaros.application.dto;

import xvgroup.icaros.domain.entity.Notification;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String type,
        String message,
        UserResponse actor,
        boolean read,
        Instant createdAt
) {
    public static NotificationResponse fromEntity(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType().name(),
                notification.getMessage(),
                UserResponse.fromEntity(notification.getActor()),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}

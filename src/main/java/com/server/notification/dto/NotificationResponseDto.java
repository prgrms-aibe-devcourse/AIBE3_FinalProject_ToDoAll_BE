package com.server.notification.dto;

import com.server.notification.domain.Notification;
import com.server.notification.domain.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponseDto (
        Long notificationId,
        NotificationType type,
        String title,
        String message,
        String payload,
        boolean readFlag,
        LocalDateTime createdAt
) {
    public static NotificationResponseDto from(Notification notification) {
        return new NotificationResponseDto(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getPayload(),   // String(JSON)
                notification.isRead(),
                notification.getCreatedAt()
        );
    }

}

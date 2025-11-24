package com.server.notification.dto;

import com.server.notification.domain.NotificationType;

public record NotificationRequestDto (
        Long userId,
        NotificationType type,
        String title,
        String message,
        NotificationPayload payload
) {
}

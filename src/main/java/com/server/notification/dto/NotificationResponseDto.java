package com.server.notification.dto;

import java.time.LocalDateTime;

public record NotificationResponseDto (

        Long notificationId,
        String title,
        String message,
        boolean readFlag,
        LocalDateTime createdAt
) {
}

package com.server.notification.dto;

import com.server.notification.domain.NotificationType;
import com.server.notification.payload.NotificationPayload;

import java.time.LocalDateTime;

public record NotificationRequestDto (
        Long userId,
        NotificationType type,
        String title,
        String message,
        NotificationPayload payload,
        LocalDateTime scheduledAt       // 예약 전송 시간 (null = 예약 없음)
) {
}

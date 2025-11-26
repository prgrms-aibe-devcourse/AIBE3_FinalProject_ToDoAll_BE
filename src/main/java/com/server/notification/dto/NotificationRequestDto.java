package com.server.notification.dto;

import com.server.notification.domain.NotificationType;

import java.time.LocalDateTime;

public record NotificationRequestDto (
        Long userId,
        NotificationType type,
        String title,
        String message,
        NotificationPayload payload,

        boolean sendNow,                // 즉시 전송 여부
        LocalDateTime scheduledAt       // 예약 전송 시간 (null = 예약 없음)
) {
}

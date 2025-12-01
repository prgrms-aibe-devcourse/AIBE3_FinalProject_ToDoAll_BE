package com.server.notification.scheduler;

import com.server.notification.domain.Notification;
import com.server.notification.repository.NotificationRepository;
import com.server.notification.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationRepository notificationRepository;
    private final SseService sseService;

    @Scheduled(fixedRate = 60000, initialDelay = 10000) // 10초 간격
    @Transactional
    public void sendScheduledNotifications() {

        LocalDateTime now = LocalDateTime.now();

        List<Notification> notifications =
                notificationRepository.findBySentFalseAndScheduledAtBefore(now);

        log.info("Scheduled notifications found: {}", notifications.size());

        for (Notification notification : notifications) {
            try {
                // SSE 발송 시도
                sseService.sendNotification(notification);

                // 성공 시 sent 상태 갱신
                notification.markSent();

            } catch (Exception e) {
                log.warn("예약 알림 발송 실패 ID: {}", notification.getId(), e);
                // 실패해도 다음 알림 처리 계속함
            }
        }
    }

    //생성된지 30일 지난 알림 삭제
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoSoftDeleteOldNotifications() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);

        List<Notification> oldReadNotifications =
                notificationRepository.findByReadTrueAndCreatedAtBeforeAndDeletedAtIsNull(threshold);

        notificationRepository.deleteAll(oldReadNotifications);
    }

}

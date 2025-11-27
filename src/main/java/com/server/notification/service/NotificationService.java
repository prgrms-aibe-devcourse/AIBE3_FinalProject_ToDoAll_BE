package com.server.notification.service;

import com.server.global.exception.ApplicationException;
import com.server.notification.domain.Notification;
import com.server.notification.dto.NotificationRequestDto;
import com.server.notification.dto.NotificationResponseDto;
import com.server.notification.exception.NotificationErrorCase;
import com.server.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // Notification 저장
    // AFTER_COMMIT 뒤에는 REQUIRES_NEW 권장
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Notification saveNotification(NotificationRequestDto dto) {
        Notification notification = Notification.of(
            dto.userId(),
            dto.type(),
            dto.title(),
            dto.message(),
            dto.payload(),
            dto.scheduledAt()
        );
        notificationRepository.save(notification);
        return notification;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getNotifications(Long userId) {

        //userId에 해당하는 알림들 최신순으로 다건 조회
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return notifications.stream()
                .map(NotificationResponseDto::from)
                .toList();
    }

    @Transactional
    public void markRead(Long notificationId, Long currentUserId) {

        //알림 단건 조회
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ApplicationException(NotificationErrorCase.NOTIFICATION_NOT_FOUND));

        if (!notification.getUserId().equals(currentUserId)) {
            throw new ApplicationException(NotificationErrorCase.FORBIDDEN);
        }
        // 읽음 표시
        notification.markRead();
    }

    @Transactional
    public void deleteNotification(Long notificationId, Long currentUserId) {

        Notification notification  = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ApplicationException(NotificationErrorCase.NOTIFICATION_NOT_FOUND));

        if (!notification.getUserId().equals(currentUserId)) {
            throw new ApplicationException(NotificationErrorCase.FORBIDDEN);
        }

        notificationRepository.delete(notification);
    }
}
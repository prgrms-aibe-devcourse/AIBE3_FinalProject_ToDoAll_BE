package com.server.interview.event;

import com.server.interview.repository.InterviewParticipantRepository;
import com.server.notification.domain.Notification;
import com.server.notification.domain.NotificationType;
import com.server.notification.dto.NotificationRequestDto;
import com.server.notification.payload.InterviewPayload;
import com.server.notification.service.NotificationService;
import com.server.notification.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InterviewEventListener {

    private final NotificationService notificationService;
    private final InterviewParticipantRepository participantRepository;
    private final SseService sseService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleInterviewCreated(InterviewCreatedEvent event) {

        List<Long> userIds = participantRepository.findUserIdsByInterviewId(event.interviewId());

        for (Long userId : userIds) {
            try {
                Notification notification = notificationService.saveNotification(
                        new NotificationRequestDto(
                                userId,
                                NotificationType.INTERVIEW,
                                "면접 생성 알림",
                                "새로운 면접이 생성되었습니다!",
                                new InterviewPayload(event.interviewId()),
                                true,
                                null
                        )
                );

                // 저장 성공한 경우에만 SSE 발송
                sseService.sendNotification(notification);

            } catch (Exception e) {
                log.error("Notification 저장 실패 - userId: {}", userId, e);
            }
        }
    }
}
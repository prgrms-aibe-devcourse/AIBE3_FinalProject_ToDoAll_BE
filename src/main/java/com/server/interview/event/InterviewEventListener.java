package com.server.interview.event;

import com.server.interview.repository.InterviewParticipantRepository;
import com.server.notification.domain.Notification;
import com.server.notification.domain.NotificationType;
import com.server.notification.dto.NotificationRequestDto;
import com.server.notification.dto.NotificationResponseDto;
import com.server.notification.payload.InterviewPayload;
import com.server.notification.service.NotificationService;
import com.server.notification.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
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
        LocalDateTime interviewAt = event.interviewAt();

        for (Long userId : userIds) {
            try {
                // 즉시 알림
                Notification notification = notificationService.saveNotification(new NotificationRequestDto(
                        userId,
                        NotificationType.INTERVIEW,
                        "면접 생성 알림",
                        "새로운 면접이 생성되었습니다!\n\uD83D\uDC49 질문 노트 바로가기",
                        new InterviewPayload(event.interviewId()),
                        null
                ));
                // 저장 성공한 경우에만 SSE 발송
                NotificationResponseDto notificationResponseDto = NotificationResponseDto.from(notification);
                sseService.sendNotification(notificationResponseDto, userId);

                // D-1 예약 알림
                notificationService.saveNotification(new NotificationRequestDto(
                        userId,
                        NotificationType.INTERVIEW,
                        "면접 하루 전 알림",
                        "내일 면접이 예정되어 있어요!\n\uD83D\uDC49 질문 노트 바로가기",
                        new InterviewPayload(event.interviewId()),
                        interviewAt.minusDays(1)
                ));

                // D-Day 1시간 전 예약 알림
                notificationService.saveNotification(new NotificationRequestDto(
                        userId,
                        NotificationType.INTERVIEW,
                        "면접 준비 알림",
                        "면접이 곧 시작됩니다. 준비해주세요!\n\uD83D\uDC49 질문 노트 바로가기",
                        new InterviewPayload(event.interviewId()),
                        interviewAt.minusHours(1)
                ));

            } catch (Exception e) {
                log.error("Notification 저장 실패 - userId: {}", userId, e);
            }
        }
    }
}
package com.server.interview.event;

import com.server.interview.repository.InterviewParticipantRepository;
import com.server.notification.domain.NotificationType;
import com.server.notification.dto.InterviewPayload;
import com.server.notification.dto.NotificationRequestDto;
import com.server.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InterviewEventListener {

    private final NotificationService notificationService;
    private final InterviewParticipantRepository participantRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleInterviewCreated(InterviewCreatedEvent event) {

        List<Long> userIds = participantRepository.findUserIdsByInterviewId(event.interviewId());

        for (Long userId : userIds) {
            notificationService.notifyUser(
                    new NotificationRequestDto(
                            userId,
                            NotificationType.INTERVIEW,
                            "면접 생성 알림",
                            "새로운 면접이 생성되었습니다!",
                            new InterviewPayload(event.interviewId())
                    )
            );
        }
    }
}
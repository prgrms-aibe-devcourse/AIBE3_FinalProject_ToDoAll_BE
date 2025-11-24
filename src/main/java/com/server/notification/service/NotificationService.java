package com.server.notification.service;

import com.server.global.exception.ApplicationException;
import com.server.notification.domain.Notification;
import com.server.notification.dto.NotificationRequestDto;
import com.server.notification.dto.NotificationResponseDto;
import com.server.notification.event.NotificationCreatedEvent;
import com.server.notification.exception.NotificationErrorCase;
import com.server.notification.repository.EmitterRepository;
import com.server.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmitterRepository emitterRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationRepository notificationRepository;

    private static final Long TIMEOUT = 1000L * 60 * 60; // 1시간

    // 클라이언트가 구독할 때 호출
    @Transactional
    public SseEmitter subscribe(Long userId) {

        SseEmitter emitter = new SseEmitter(TIMEOUT);

        // 저장
        emitterRepository.save(userId, emitter);

        // 연결 종료되면 emitter 제거
        emitter.onCompletion(() -> emitterRepository.delete(userId)); //정상적으로 종료가 되면 연결을 끊는다.
        emitter.onTimeout(() -> emitterRepository.delete(userId)); //시간이 초과되면 연결을 끊는다.
        emitter.onError((e) -> emitterRepository.delete(userId)); //에러가 발생하면 연결을 끊는다.

        // 최초 연결 시 “connected” 이벤트 보내기
        sendInternal(emitter);

        return emitter; // 리턴이 끝나야 emitter에 HTTP OutputStream이 저장됨
    }

    // 알림 생성 + DB 저장 + SSE 실시간
    // DB 저장 + 이벤트 발행
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyUser(NotificationRequestDto dto) {

        // 1) DB 저장
        Notification saved = Notification.of(
                dto.userId(),
                dto.type(),
                dto.title(),
                dto.message(),
                dto.payload()
        );
        notificationRepository.save(saved);

        // 2) 이벤트 발행 (트랜잭션 종료 후 SSE 전송)
        eventPublisher.publishEvent(new NotificationCreatedEvent(saved));
    }

    // SSE 전송 (트랜잭션 바깥)
    @Transactional
    public void sendSse(Notification notification) {

        Long userId = notification.getUserId();

        if (!emitterRepository.hasEmitter(userId)) {
            return; // SSE 구독 안한 상태일 수도 있음
        }

        SseEmitter emitter = emitterRepository.get(userId);

        NotificationResponseDto responseDto = NotificationResponseDto.from(notification);

        try {
            emitter.send(
                    SseEmitter.event()
                            .name("notification")
                            .data(responseDto)
            );
        } catch (Exception e) {
            emitterRepository.delete(userId);
        }
    }

    // 내부용
    private void sendInternal(SseEmitter emitter) {
        try {
            //send()는 “전송 요청”을 emitter 내부 큐에 쓰기만 한다. 추후 outputstream이 저장되고 flush될 때 실질적인 전송이 발생
            emitter.send(SseEmitter.event().data((Object) "connected"));
        } catch (Exception e) {
            log.warn("SSE 연결 실패 또는 클라이언트 종료", e);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getNotifications(Long userId) {

        // 테스트를 위해 하드코딩 나중에 삭제
        userId = 1L;

        //userId에 해당하는 알림들 최신순으로 다건 조회
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return notifications.stream()
                .map(n -> new NotificationResponseDto(
                        n.getId(),
                        n.getType(),
                        n.getTitle(),
                        n.getMessage(),
                        n.getPayload(),
                        n.isReadFlag(),
                        n.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public void markRead(Long notificationId) {
        //알림 단건 조회
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ApplicationException(NotificationErrorCase.NOTIFICATION_NOT_FOUND));

        // 읽음 표시
        notification.markRead();
    }
}
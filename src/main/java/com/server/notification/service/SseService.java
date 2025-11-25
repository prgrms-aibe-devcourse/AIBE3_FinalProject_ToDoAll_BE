package com.server.notification.service;

import com.server.notification.domain.Notification;
import com.server.notification.dto.NotificationResponseDto;
import com.server.notification.repository.EmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseService {
    private final EmitterRepository emitterRepository;

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

    public void sendNotification(Notification notification) {
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
            log.warn("SSE 전송 실패", e);
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
}
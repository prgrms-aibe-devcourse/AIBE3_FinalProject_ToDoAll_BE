package com.server.notification.service;

import com.server.interview.repository.InterviewParticipantRepository;
import com.server.notification.dto.NotificationDto;
import com.server.notification.repository.EmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmitterRepository emitterRepository;
    private final InterviewParticipantRepository participantRepository;

    private static final Long TIMEOUT = 1000L * 60 * 60; // 1시간

    // 클라이언트가 구독할 때 호출
    public SseEmitter subscribe(Long userId) {

        SseEmitter emitter = new SseEmitter(TIMEOUT);

        // 저장
        emitterRepository.save(userId, emitter);

        // 연결 종료되면 emitter 제거
        emitter.onCompletion(() -> emitterRepository.delete(userId)); //정상적으로 종료가 되면 연결을 끊는다.
        emitter.onTimeout(() -> emitterRepository.delete(userId)); //시간이 초과되면 연결을 끊는다.
        emitter.onError((e) -> emitterRepository.delete(userId)); //에러가 발생하면 연결을 끊는다.

        // 최초 연결 시 “connected” 이벤트 보내기
        sendInternal(emitter, "connected");

        return emitter; // 리턴이 끝나야 emitter에 HTTP OutputStream이 저장됨
    }

    // 서버에서 클라이언트에게 알림 보내는 함수
    public void send(Long userId, NotificationDto notification) {

        SseEmitter emitter = emitterRepository.get(userId);

        if (emitter == null) {
            log.warn("No active SSE connection for user {}", userId);
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(notification)
            );
        } catch (Exception e) {
            emitterRepository.delete(userId); // 끊어진 emitter 정리
        }
    }

    public void sendToInterviewParticipants(Long interviewId, NotificationDto dto) {
        // 인터뷰 참여자 userId 목록 조회
        List<Long> participantIds = participantRepository.findUserIdsByInterviewId(interviewId);

        for (Long userId : participantIds) {
            send(userId, dto);
        }
    }


    // 내부용
    private void sendInternal(SseEmitter emitter, Object data) {
        try {
            //send()는 “전송 요청”을 emitter 내부 큐에 쓰기만 한다. 추후 outputstream이 저장되고 flush될 때 실질적인 전송이 발생
            emitter.send(SseEmitter.event().data(data));
        } catch (Exception e) {
            log.warn("SSE 연결 실패 또는 클라이언트 종료", e);
        }
    }
}
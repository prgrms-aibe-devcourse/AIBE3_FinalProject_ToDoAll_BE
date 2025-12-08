package com.server.notification.service;

import com.server.notification.domain.NotificationType;
import com.server.notification.dto.NotificationResponseDto;
import com.server.notification.repository.EmitterRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SseServiceTest {

    @Mock
    private EmitterRepository emitterRepository;

    @Mock
    private SseEmitter emitter;

    @InjectMocks
    private SseService sseService;

    @Test
    @DisplayName("Emitter가 존재하면 SSE 이벤트를 전송한다")
    void sendNotification_success() throws Exception {
        // Given
        Long userId = 1L;
        NotificationResponseDto dto = new NotificationResponseDto(
                100L,
                NotificationType.INTERVIEW,
                "제목",
                "메시지",
                "{\"interviewId\":10}",
                false,
                LocalDateTime.now()
        );

        when(emitterRepository.hasEmitter(userId)).thenReturn(true);
        when(emitterRepository.get(userId)).thenReturn(emitter);

        // When
        sseService.sendNotification(dto, userId);

        // Then
        verify(emitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));

    }

    @Test
    @DisplayName("Emitter가 없으면 아무 동작도 하지 않는다")
    void sendNotification_noEmitter() {
        Long userId = 2L;
        NotificationResponseDto dto = new NotificationResponseDto(
                200L, NotificationType.INTERVIEW, "t", "m",
                "{}", false, LocalDateTime.now()
        );

        when(emitterRepository.hasEmitter(userId)).thenReturn(false);

        sseService.sendNotification(dto, userId);

        verify(emitterRepository, never()).get(any());
        verifyNoInteractions(emitter);
    }

    @Test
    @DisplayName("전송 실패 시 Emitter를 삭제한다")
    void sendNotification_failure() throws Exception {
        Long userId = 3L;
        NotificationResponseDto dto = new NotificationResponseDto(
                300L, NotificationType.INTERVIEW, "fail", "fail-msg",
                "{}", false, LocalDateTime.now()
        );

        when(emitterRepository.hasEmitter(userId)).thenReturn(true);
        when(emitterRepository.get(userId)).thenReturn(emitter);

        doThrow(new RuntimeException("전송 실패"))
                .when(emitter).send(any(SseEmitter.SseEventBuilder.class));

        sseService.sendNotification(dto, userId);

        verify(emitterRepository, times(1)).delete(userId);
    }
}


package com.server.notification.controller;

import com.server.notification.dto.NotificationDto;
import com.server.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api//v1/notifications")
@RequiredArgsConstructor
@Tag(name = "NotificationController", description = "API SSE 통신 알림 컨트롤러")
public class NotificationController {

    private final NotificationService notificationService;

    // SSE 구독 API
    @GetMapping(value = "/subscribe", produces = "text/event-stream")
    @Operation(summary = "SSE 구독", description = "SSE를 구독하여 Emitter를 생성합니다.")
    public SseEmitter subscribe(
            @RequestParam Long userId
    ) {
        return notificationService.subscribe(userId);
    }

    // 테스트용: 서버 → 클라 알림 보내기 실제 환경에서는 사용되지 않는 api
    @PostMapping("/send")
    @Operation(summary = "SSE 알림 전송 테스트 api", description = "서버에서 클라이언트로 알림이 정상적으로 보내는지 확인하는 test api입니다.")
    public void send(
            @RequestParam Long userId,
            @RequestBody NotificationDto dto
    ) {
        notificationService.send(userId, dto);
    }
}
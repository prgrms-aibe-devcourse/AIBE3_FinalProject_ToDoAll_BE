package com.server.notification.controller;

import com.server.global.response.CommonResponse;
import com.server.notification.dto.NotificationRequestDto;
import com.server.notification.dto.NotificationResponseDto;
import com.server.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "NotificationController", description = "API SSE 통신 알림 컨트롤러")
public class NotificationController {

    private final NotificationService notificationService;

    // SSE 구독 API
    // SSE는 produces = "text/event-stream"
    // 브라우저는 **응답의 Content-Type이 text/event-stream**이어야
    // 이걸 SSE로 인식하고 스트리밍 연결을 유지함.
    @GetMapping(value = "/subscribe", produces = "text/event-stream")
    @Operation(summary = "SSE 구독", description = "SSE를 구독하여 Emitter를 생성합니다.")
    public SseEmitter subscribe(
            @RequestParam Long userId
    ) {
        return notificationService.subscribe(userId);
    }

    // 알림 조회
    @GetMapping("")
    @Operation(summary = "알림 다건 조회 api", description = "유저의 알림 다건 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 조회 성공")
    })
    public CommonResponse<List<NotificationResponseDto>> getNotifications(
            @AuthenticationPrincipal Long userId
    ) {
        List<NotificationResponseDto> response = notificationService.getNotifications(userId);
        return CommonResponse.success(response);
    }

    // 읽음 처리
    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "알림 읽음 처리 api", description = "읽은 알림의 상태 변경")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 상태 변경 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 알림")
    })
    public CommonResponse<String> markRead(
            @PathVariable Long notificationId
    ) {
        notificationService.markRead(notificationId);

        return CommonResponse.success("읽음 처리 성공");

    }

    // 테스트용: 서버 → 클라 알림 보내기 실제 환경에서는 사용되지 않는 api
    @PostMapping("/send")
    @Operation(summary = "SSE 알림 전송 테스트 api", description = "서버에서 클라이언트로 알림이 정상적으로 보내는지 확인하는 test api입니다.")
    public void send(
            @RequestBody NotificationRequestDto dto
    ) {
        notificationService.notifyUser(dto);
    }
}
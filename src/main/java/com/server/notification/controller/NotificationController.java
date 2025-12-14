package com.server.notification.controller;

import com.server.auth.exception.AuthErrorCase;
import com.server.global.config.security.jwt.JwtTokenProvider;
import com.server.global.exception.ApplicationException;
import com.server.global.response.CommonResponse;
import com.server.notification.dto.NotificationResponseDto;
import com.server.notification.service.NotificationService;
import com.server.notification.service.SseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "NotificationController", description = "API SSE 통신 알림 컨트롤러")
public class NotificationController {

    private final NotificationService notificationService;
    private final SseService sseService;
    private final JwtTokenProvider jwtTokenProvider;

    // SSE 구독 API
    // SSE는 produces = "text/event-stream"
    // 브라우저는 **응답의 Content-Type이 text/event-stream**이어야
    // 이걸 SSE로 인식하고 스트리밍 연결을 유지함.
    @GetMapping(value = "/subscribe", produces = "text/event-stream")
    @Operation(summary = "SSE 구독", description = "SSE를 구독하여 Emitter를 생성합니다.")
    public SseEmitter subscribe(@RequestParam("token") String token) {

        if (token == null || !jwtTokenProvider.validateToken(token)) {
            throw new ApplicationException(AuthErrorCase.AUTH_INVALID_TOKEN);
        }

        Long userId = jwtTokenProvider.getUserId(token);
        return sseService.subscribe(userId);
    }

    // 알림 조회
    @GetMapping("")
    @Operation(summary = "알림 다건 조회 api", description = "유저의 알림 다건 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 조회 성공")
    })
    public CommonResponse<List<NotificationResponseDto>> getNotifications(
    ) {
        List<NotificationResponseDto> response = notificationService.getNotifications();
        return CommonResponse.success(response);
    }

    // 읽음 처리
    @PatchMapping("/read")
    @Operation(summary = "알림 읽음 처리 api", description = "알림창의 모든 알림 읽음으로 상태 변경")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 상태 변경 성공")
    })
    public CommonResponse<String> markRead() {
        notificationService.markRead();
        return CommonResponse.success("읽음 처리 성공");
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "알림 삭제 api", description = "알림 수동 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 알림")
    })
    public CommonResponse<String> deleteNotification(
            @PathVariable Long notificationId
    ) {
        notificationService.deleteNotification(notificationId);
        return CommonResponse.success("알림 삭제 성공");
    }

    @DeleteMapping("")
    @Operation(summary = "내 알림 전체 삭제 api", description = "자신에게 온 알림을 전체 삭제")
    public CommonResponse<String> deleteAllNotification() {
        notificationService.deleteAllNotification();
        return CommonResponse.success("알림 삭제 성공");
    }
}
package com.server.auth.controller;

import com.server.auth.dto.*;
import com.server.auth.service.AuthService;
import com.server.auth.service.PasswordResetService;
import com.server.global.response.CommonResponse;
import com.server.auth.dto.PasswordResetConfirmRequestDto;
import com.server.user.dto.UserLoginRequestDto;
import com.server.user.dto.UserLoginResponseDto;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    // 비밀번호 재설정 이메일 발송

    @Operation(
            summary = "비밀번호 재설정 이메일 발송",
            description = "입력한 이메일로 비밀번호 재설정 링크를 발송합니다. (유효시간: 30분)"
    )
    @PostMapping("/password/reset-requests")
    public CommonResponse<PasswordResetResponseDto> sendPasswordResetEmail(
            @Valid @RequestBody PasswordResetRequestDto request
    ) {
        // Service가 ResponseDto 반환
        PasswordResetResponseDto response = passwordResetService.sendPasswordResetEmail(request);
        return CommonResponse.success(response);
    }

    //비밀번호 재설정 실행

    @Operation(
            summary = "비밀번호 재설정 실행",
            description = "이메일로 받은 토큰과 새 비밀번호로 비밀번호를 변경합니다."
    )
    @PostMapping("/password/reset")
    public CommonResponse<PasswordResetResponseDto> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmRequestDto request
    ) {
        // DTO만 전달, 내부 의존성은 Service가 처리
        PasswordResetResponseDto response = passwordResetService.resetPassword(request);
        return CommonResponse.success(response);
    }


     //로그인
    @PostMapping("/token")
    public CommonResponse<UserLoginResponseDto> login(
            @Valid @RequestBody UserLoginRequestDto request
    ) {
        UserLoginResponseDto response = authService.login(
                request.email(),
                request.password()
        );
        return CommonResponse.success(response);
    }

    @PostMapping("/token/refresh")
    public CommonResponse<UserLoginResponseDto> refresh(
            @Valid @RequestBody TokenRefreshRequestDto request
    ) {
        UserLoginResponseDto response = authService.reissueAccessToken(
                request.refreshToken()
        );
        return CommonResponse.success(response);
    }

    //로그아웃
    @PostMapping("/token")
    public CommonResponse<String> logout(
            @Valid @RequestBody TokenRefreshRequestDto request
    ) {
        // 실제 쿠키 삭제 등의 로그아웃 처리 로직은 서비스에 위임
        authService.logout(request.refreshToken());

        // 로그아웃은 별도의 데이터가 필요 없으므로 data = null로 응답
        return CommonResponse.success("로그아웃 되었습니다.");
    }
}

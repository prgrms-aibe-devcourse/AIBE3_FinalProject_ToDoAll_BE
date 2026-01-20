package com.server.auth.controller;


import com.server.auth.config.AuthCookieProperties;
import com.server.auth.dto.*;
import com.server.auth.service.AuthService;
import com.server.auth.service.PasswordResetService;
import com.server.global.response.CommonResponse;
import com.server.auth.dto.PasswordResetConfirmRequestDto;
import com.server.user.dto.UserLoginRequestDto;
import com.server.user.dto.UserLoginResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
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

    private final AuthCookieProperties authCookieProperties;

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
    @Operation(
            summary = "로그인 및 토큰 발급")
    @PostMapping("/token")
    public CommonResponse<UserLoginResponseDto> login(
            @Valid @RequestBody UserLoginRequestDto request,
            HttpServletResponse httpServletResponse
    ) {
        UserLoginResponseDto response = authService.login(
                request.email(),
                request.password()
        );

        // Access Token 쿠키
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", response.getAccessToken())
                .httpOnly(true)
                .secure(authCookieProperties.isSecure())
                .sameSite(authCookieProperties.getSameSite())
                .path(authCookieProperties.getPath())
                .maxAge(authCookieProperties.getAccessMaxAgeSeconds())
                .build();

        // Refresh Token 쿠키
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
                .httpOnly(true)
                .secure(authCookieProperties.isSecure())
                .sameSite(authCookieProperties.getSameSite())
                .path(authCookieProperties.getPath())
                .maxAge(authCookieProperties.getRefreshMaxAgeSeconds())
                .build();

        httpServletResponse.addHeader("Set-Cookie", accessCookie.toString());
        httpServletResponse.addHeader("Set-Cookie", refreshCookie.toString());

        UserLoginResponseDto body = UserLoginResponseDto.of(response.getAccessToken(), null);


        return CommonResponse.success(body);
    }
    @Operation(
            summary = "Access Token 재발급")
    @PostMapping("/token/refresh")
    public CommonResponse<UserLoginResponseDto> refresh(
            // 쿠키 기반으로 바꾸기 위해 Request/Response를 받음
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) {
        // refreshToken을 요청 바디가 아니라 쿠키에서 꺼냄
        String refreshToken = extractCookie(httpServletRequest, "refreshToken");

        // 쿠키에서 꺼낸 refreshToken으로 재발급
        UserLoginResponseDto response = authService.reissueAccessToken(refreshToken);

        // Access 쿠키
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", response.getAccessToken())
                .httpOnly(true)
                .secure(authCookieProperties.isSecure())
                .sameSite(authCookieProperties.getSameSite())
                .path(authCookieProperties.getPath())
                .maxAge(authCookieProperties.getAccessMaxAgeSeconds())
                .build();

        // Refresh 쿠키
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
                .httpOnly(true)
                .secure(authCookieProperties.isSecure())
                .sameSite(authCookieProperties.getSameSite())
                .path(authCookieProperties.getPath())
                .maxAge(authCookieProperties.getRefreshMaxAgeSeconds())
                .build();

        httpServletResponse.addHeader("Set-Cookie", accessCookie.toString());
        httpServletResponse.addHeader("Set-Cookie", refreshCookie.toString());

        UserLoginResponseDto body = UserLoginResponseDto.of(response.getAccessToken(), null);

        return CommonResponse.success(body);
    }


    //로그아웃
    @Operation(
            summary = "로그아웃")
    @PostMapping("/logout")
    public CommonResponse<String> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // 요청 쿠키에서 refreshToken 꺼내기
        String refreshToken = extractCookie(request, "refreshToken");

        // 실제 쿠키 삭제 등의 로그아웃 처리 로직은 서비스에 위임
        authService.logout(refreshToken);

        // 쿠키 삭제 (accessToken / refreshToken 둘 다 만료시킴)
        ResponseCookie clearAccess = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(authCookieProperties.isSecure())
                .sameSite(authCookieProperties.getSameSite())
                .path(authCookieProperties.getPath())
                .maxAge(0)
                .build();

        ResponseCookie clearRefresh = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(authCookieProperties.isSecure())
                .sameSite(authCookieProperties.getSameSite())
                .path(authCookieProperties.getPath())
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", clearAccess.toString());
        response.addHeader("Set-Cookie", clearRefresh.toString());


        // 로그아웃은 별도의 데이터가 필요 없으므로 data = null로 응답
        return CommonResponse.success("로그아웃 되었습니다.");
    }

    // 요청 쿠키에서 name 에 해당하는 쿠키 값을 꺼내는 헬퍼 메서드

    private String extractCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}

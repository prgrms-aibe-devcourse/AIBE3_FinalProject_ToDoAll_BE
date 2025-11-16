package com.server.auth.controller;

import com.server.auth.dto.TokenRefreshRequestDto;
import com.server.auth.service.AuthService;
import com.server.global.response.CommonResponse;
import com.server.user.dto.UserLoginRequestDto;
import com.server.user.dto.UserLoginResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    @PostMapping("/logout")
    public CommonResponse<String> logout(
            @Valid @RequestBody TokenRefreshRequestDto request
    ) {
        // 실제 쿠키 삭제 등의 로그아웃 처리 로직은 서비스에 위임
        authService.logout(request.refreshToken());

        // 로그아웃은 별도의 데이터가 필요 없으므로 data = null로 응답
        return CommonResponse.success("로그아웃 되었습니다.");
    }
}

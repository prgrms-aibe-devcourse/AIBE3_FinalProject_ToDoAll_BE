package com.server.auth.controller;

import com.server.auth.service.AuthService;
import com.server.global.response.CommonResponse;
import com.server.user.dto.UserLoginRequestDto;
import com.server.user.dto.UserLoginResponseDto;
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
}

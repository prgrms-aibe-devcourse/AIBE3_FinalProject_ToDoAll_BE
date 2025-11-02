package com.server.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    // TODO: 로그인,로그아웃,토큰 재발급 등 인증 관련 컨트롤러 로직 구현
}

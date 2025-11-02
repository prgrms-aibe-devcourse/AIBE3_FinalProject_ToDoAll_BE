package com.server.auth.service;

import com.server.user.dto.UserLoginResponseDto;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public UserLoginResponseDto login(String email, String password) {
        // TODO: 사용자 인증, JWT 발급
        return null;
    }

    public void logout(String refreshToken) {
        // TODO: Redis에서 리프레시 토큰 삭제
    }

    public UserLoginResponseDto reissueAccessToken(String refreshToken) {
        // TODO: 리프레시 토큰 검증 & 액세스 토큰 재발급
        return null;
    }
}

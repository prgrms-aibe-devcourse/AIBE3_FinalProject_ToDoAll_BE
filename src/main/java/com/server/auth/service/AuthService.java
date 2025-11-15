package com.server.auth.service;

import com.server.auth.exception.AuthErrorCase;
import com.server.global.config.security.jwt.JwtTokenProvider;
import com.server.global.exception.ApplicationException;
import com.server.user.domain.User;
import com.server.user.dto.UserLoginResponseDto;
import com.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserLoginResponseDto login(String email, String password) {
        // 1) 이메일로 사용자 찾기
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("로그인 실패 -> 존재하지 않는 이메일: {}", email);
                    return ApplicationException.from(AuthErrorCase.AUTH_INVALID_CREDENTIAL);
                });

        // 2) 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("로그인 실패 -> 비밀번호 불일치: {}", email);
            throw ApplicationException.from(AuthErrorCase.AUTH_INVALID_CREDENTIAL);
        }

        // 3) JWT 토큰 2개 생성
        String accessToken = jwtTokenProvider.generateToken(user.getId(), "ROLE_USER");
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        log.info("로그인 성공");

        // 4) 응답 DTO 만들어서 리턴
        return UserLoginResponseDto.of(accessToken, refreshToken);
    }


    public void logout(String refreshToken) {
        // TODO: Redis에서 리프레시 토큰 삭제
    }

    public UserLoginResponseDto reissueAccessToken(String refreshToken) {
        // TODO: 리프레시 토큰 검증 & 액세스 토큰 재발급
        return null;
    }
}

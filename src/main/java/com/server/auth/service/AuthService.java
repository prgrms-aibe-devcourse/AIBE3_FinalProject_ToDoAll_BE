package com.server.auth.service;

import com.server.auth.exception.AuthErrorCase;
import com.server.global.config.security.jwt.JwtProperties;
import com.server.global.config.security.jwt.JwtTokenProvider;
import com.server.global.exception.ApplicationException;
import com.server.user.domain.User;
import com.server.user.dto.UserLoginResponseDto;
import com.server.user.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    // Redis에 저장할 때 사용할 키 prefix
    private static final String REFRESH_TOKEN_KEY_PREFIX = "RT:";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate; // 리프레시 토큰 저장소
    private final JwtProperties jwtProperties; // jwt.secret, jwt.refresh-exp 접근용
    private final EmailAuthService emailAuthService;  // 이메일 보내는 서비스



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

        // 4) Redis에 리프레시 토큰 저장
        String redisKey = getRefreshTokenKey(user.getId());        // RT:{userId}
        long refreshExpMillis = jwtProperties.getRefreshExp();     // yml 에서 설정한 만료(ms)
        redisTemplate.opsForValue()
                .set(redisKey, refreshToken, refreshExpMillis, TimeUnit.MILLISECONDS);


        log.info("로그인 성공");

        // 4) 응답 DTO 만들어서 리턴
        return UserLoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


    public void logout(String refreshToken) {
        // 토큰이 아예 안 왔으면 그냥 종료 (여러 번 호출해도 문제 없게)
        if (refreshToken == null || refreshToken.isBlank()) {
            log.info("로그아웃 요청 - refreshToken 없음, 아무 작업 없이 종료");
            return;
        }

        // 1) 토큰 유효성 확인 (만료/서명 검증)
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("로그아웃 요청 - 유효하지 않은 리프레시 토큰, 그냥 무시");
            // UX 위해 예외 던지지 않고 종료
            return;
        }

        // 2) 토큰에서 userId 추출
        Long userId = parseUserIdFromToken(refreshToken);
        String redisKey = getRefreshTokenKey(userId);

        // 3) Redis 에서 리프레시 토큰 삭제
        redisTemplate.delete(redisKey);
        log.info("로그아웃 완료: userId={}, redisKey 삭제={}", userId, redisKey);
    }

    public UserLoginResponseDto reissueAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw ApplicationException.from(AuthErrorCase.AUTH_INVALID_CREDENTIAL);
        }

        // 1) 토큰 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("토큰 재발급 실패 - 유효하지 않은 리프레시 토큰");
            throw ApplicationException.from(AuthErrorCase.AUTH_INVALID_REFRESH_TOKEN);
        }

        // 2) 토큰에서 userId 추출
        Long userId = parseUserIdFromToken(refreshToken);
        String redisKey = getRefreshTokenKey(userId);

        // 3) Redis 에 저장된 리프레시 토큰과 일치하는지 확인
        Object stored = redisTemplate.opsForValue().get(redisKey);
        if (stored == null) {
            log.warn("토큰 재발급 실패 - Redis 에 리프레시 토큰이 없음, userId={}", userId);
            throw ApplicationException.from(AuthErrorCase.AUTH_INVALID_REFRESH_TOKEN);
        }
        if (!refreshToken.equals(stored.toString())) {
            log.warn("토큰 재발급 실패 - Redis 리프레시 토큰과 불일치, userId={}", userId);
            throw ApplicationException.from(AuthErrorCase.AUTH_REFRESH_TOKEN_MISMATCH);
        }

        // 4) 새 토큰 발급
        String newAccessToken = jwtTokenProvider.generateToken(userId, "ROLE_USER");
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);

        // 5) Redis 에 새 리프레시 토큰 저장
        long refreshExpMillis = jwtProperties.getRefreshExp();
        redisTemplate.opsForValue()
                .set(redisKey, newRefreshToken, refreshExpMillis, TimeUnit.MILLISECONDS);

        log.info("토큰 재발급 성공: userId={}, redisKey={}", userId, redisKey);

        // 6) 새 토큰 DTO 반환
        return UserLoginResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    private String getRefreshTokenKey(Long userId) {
        return REFRESH_TOKEN_KEY_PREFIX + userId;
    }

    private Long parseUserIdFromToken(String token) {
        try {
            String secret = jwtProperties.getSecret();// 시크릿 문자열
            Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); // 서명 키 생성

            String subject = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject(); // subject = userId

            return Long.parseLong(subject);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("토큰에서 userId 추출 실패", e);
            throw ApplicationException.from(AuthErrorCase.AUTH_INVALID_CREDENTIAL);
        }
    }
}

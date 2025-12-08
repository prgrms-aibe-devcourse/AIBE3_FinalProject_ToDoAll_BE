package com.server.auth.service;

import com.server.auth.exception.AuthErrorCase;
import com.server.global.config.security.jwt.JwtProperties;
import com.server.global.config.security.jwt.JwtTokenProvider;
import com.server.global.exception.ApplicationException;
import com.server.user.domain.TestFixtures;
import com.server.user.domain.User;
import com.server.user.dto.UserLoginResponseDto;
import com.server.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @Mock
    RedisTemplate<String, Object> redisTemplate;

    @Mock
    JwtProperties jwtProperties;

    @Mock
    EmailAuthService emailAuthService;

    @InjectMocks
    AuthService authService;

    @Test
    @DisplayName("로그인 성공 - 토큰 2개 발급 및 Redis 저장")
    void login_success() {
        // given
        String email = "test@company.com";
        String rawPassword = "Password1!";

        User user = TestFixtures.createUser(email, "홍길동");
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "password", "encoded-password");

        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(rawPassword, "encoded-password")).willReturn(true);
        given(jwtTokenProvider.generateToken(1L, "ROLE_USER")).willReturn("access-token");
        given(jwtTokenProvider.generateRefreshToken(1L)).willReturn("refresh-token");
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        given(jwtProperties.getRefreshExp()).willReturn(1000L * 60 * 60 * 24 * 7); // 7일

        UserLoginResponseDto result = authService.login(email, rawPassword);

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");

        verify(valueOps).set(eq("RT:1"), eq("refresh-token"), anyLong(), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("로그인 실패")
    void login_emailNotFound_throws() {
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        ApplicationException ex = assertThrows(ApplicationException.class,
                () -> authService.login("notfound@company.com", "pw"));

        assertThat(ex.getErrorCase()).isEqualTo(AuthErrorCase.AUTH_EMAIL_NOT_FOUND);
    }

    @Test
    @DisplayName("로그인 실패")
    void login_wrongPassword_throws() {
        String email = "test@company.com";

        User user = TestFixtures.createUser(email, "홍길동");
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "password", "encoded-password");

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(passwordEncoder.matches("WrongPw1!", "encoded-password")).willReturn(false);

        ApplicationException ex = assertThrows(ApplicationException.class,
                () -> authService.login(email, "WrongPw1!"));

        assertThat(ex.getErrorCase()).isEqualTo(AuthErrorCase.AUTH_PASSWORD_INCORRECT);
    }

    @Test
    @DisplayName("리프레시 토큰으로 재발급 성공")
    void reissueAccessToken_success() {
        String oldRefreshToken = "old-refresh-token";

        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);

        given(jwtTokenProvider.validateToken(oldRefreshToken)).willReturn(true);
        given(jwtTokenProvider.getUserId(oldRefreshToken)).willReturn(1L);
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        given(valueOps.get("RT:1")).willReturn(oldRefreshToken);
        given(jwtTokenProvider.generateToken(1L, "ROLE_USER")).willReturn("new-access-token");
        given(jwtTokenProvider.generateRefreshToken(1L)).willReturn("new-refresh-token");
        given(jwtProperties.getRefreshExp()).willReturn(1000L * 60 * 60 * 24 * 7);

        UserLoginResponseDto result = authService.reissueAccessToken(oldRefreshToken);

        assertThat(result.getAccessToken()).isEqualTo("new-access-token");
        assertThat(result.getRefreshToken()).isEqualTo("new-refresh-token");

        verify(valueOps).set(eq("RT:1"), eq("new-refresh-token"), anyLong(), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("리프레시 토큰 재발급 실패")
    void reissueAccessToken_invalidToken_throws() {
        String refreshToken = "invalid-token";

        given(jwtTokenProvider.validateToken(refreshToken)).willReturn(false);

        ApplicationException ex = assertThrows(ApplicationException.class,
                () -> authService.reissueAccessToken(refreshToken));

        assertThat(ex.getErrorCase()).isEqualTo(AuthErrorCase.AUTH_INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("리프레시 토큰 재발급 실패")
    void reissueAccessToken_noTokenInRedis_throws() {
        String refreshToken = "refresh-token";

        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);

        given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtTokenProvider.getUserId(refreshToken)).willReturn(1L);
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        given(valueOps.get("RT:1")).willReturn(null);

        ApplicationException ex = assertThrows(ApplicationException.class,
                () -> authService.reissueAccessToken(refreshToken));

        assertThat(ex.getErrorCase()).isEqualTo(AuthErrorCase.AUTH_INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("리프레시 토큰 재발급 실패")
    void reissueAccessToken_mismatch_throws() {
        String refreshToken = "refresh-token";

        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);

        given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtTokenProvider.getUserId(refreshToken)).willReturn(1L);
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        given(valueOps.get("RT:1")).willReturn("different-token");

        ApplicationException ex = assertThrows(ApplicationException.class,
                () -> authService.reissueAccessToken(refreshToken));

        assertThat(ex.getErrorCase()).isEqualTo(AuthErrorCase.AUTH_REFRESH_TOKEN_MISMATCH);
    }

    @Test
    @DisplayName("로그아웃")
    void logout_success() {
        String refreshToken = "refresh-token";

        given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true);
        given(jwtTokenProvider.getUserId(refreshToken)).willReturn(1L);

        authService.logout(refreshToken);

        verify(redisTemplate).delete("RT:1");
    }

    @Test
    @DisplayName("로그아웃")
    void logout_nullToken_noop() {
        authService.logout(null);
        verifyNoInteractions(jwtTokenProvider, redisTemplate);
    }
}


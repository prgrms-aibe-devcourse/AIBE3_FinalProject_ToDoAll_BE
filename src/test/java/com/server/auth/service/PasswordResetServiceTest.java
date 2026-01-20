package com.server.auth.service;

import com.server.auth.dto.PasswordResetConfirmRequestDto;
import com.server.auth.dto.PasswordResetRequestDto;
import com.server.auth.dto.PasswordResetResponseDto;
import com.server.auth.exception.AuthErrorCase;
import com.server.auth.repository.PasswordResetTokenRepository;
import com.server.global.exception.ApplicationException;
import com.server.auth.domain.PasswordResetToken;
import com.server.user.domain.User;
import com.server.user.repository.UserRepository;
import com.server.user.util.PasswordValidator;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordValidator passwordValidator;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordResetService, "expiryMinutes", 30);
        ReflectionTestUtils.setField(
                passwordResetService,
                "frontendResetUrl",
                "https://frontend.example.com/reset"
        );
    }

    // sendPasswordResetEmail

    @Test
    @DisplayName("비밀번호 재설정 이메일 발송 - 사용자가 없으면 예외 발생")
    void sendPasswordResetEmail_userNotFound() {
        String email = "unknown@example.com";
        PasswordResetRequestDto request = new PasswordResetRequestDto(email);

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        ApplicationException ex = assertThrows(
                ApplicationException.class,
                () -> passwordResetService.sendPasswordResetEmail(request)
        );

        assertThat(ex.getErrorCase()).isEqualTo(AuthErrorCase.AUTH_USER_NOT_FOUND);
        verify(tokenRepository, never()).save(any());
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("비밀번호 재설정 이메일 발송 - 정상 유저인 경우 토큰 생성 및 메일 전송")
    void sendPasswordResetEmail_success() throws Exception {
        String email = "test@example.com";
        PasswordResetRequestDto request = new PasswordResetRequestDto(email);

        User user = User.createForSignup(
                email,
                "encoded-password",
                "홍길동",
                "길동이",
                "테스트회사",
                "백엔드 개발자"
        );
        ReflectionTestUtils.setField(user, "id", 1L);

        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(tokenRepository.findLatestUnusedToken(eq(1L), any(LocalDateTime.class)))
                .willReturn(Optional.empty());

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        given(mailSender.createMimeMessage()).willReturn(mimeMessage);

        PasswordResetResponseDto response =
                passwordResetService.sendPasswordResetEmail(request);

        assertThat(response.message()).contains("비밀번호 재설정 링크가 이메일로 발송되었습니다");
        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("비밀번호 재설정 성공")
    void resetPassword_success() {
        String plainToken = "plain-token";
        String newPassword = "NewPassword1!";

        User user = mock(User.class);
        when(user.getEmail()).thenReturn("test@example.com");

        PasswordResetToken token = mock(PasswordResetToken.class);
        when(token.isUsed()).thenReturn(false);
        when(token.isExpired()).thenReturn(false);
        when(token.getUser()).thenReturn(user);

        given(tokenRepository.findByTokenHash(anyString()))
                .willReturn(Optional.of(token));

        // 비밀번호 정책 통과
        doNothing().when(passwordValidator)
                .validateForSignup(eq(newPassword), eq("test@example.com"));

        // 암호화 결과
        given(passwordEncoder.encode(newPassword))
                .willReturn("encoded-new-password");

        PasswordResetConfirmRequestDto request =
                new PasswordResetConfirmRequestDto(plainToken, newPassword);

        PasswordResetResponseDto response =
                passwordResetService.resetPassword(request);

        assertThat(response.message())
                .contains("비밀번호가 성공적으로 변경되었습니다");

        verify(passwordValidator)
                .validateForSignup(newPassword, "test@example.com");
        verify(user).changePassword("encoded-new-password");
        verify(token).markAsUsed();
    }

    @Test
    @DisplayName("비밀번호 재설정 실패 - 토큰을 찾을 수 없으면 예외 발생")
    void resetPassword_invalidToken() {
        String plainToken = "invalid-token";
        String newPassword = "NewPassword1!";
        PasswordResetConfirmRequestDto request =
                new PasswordResetConfirmRequestDto(plainToken, newPassword);

        given(tokenRepository.findByTokenHash(anyString()))
                .willReturn(Optional.empty());

        ApplicationException ex = assertThrows(
                ApplicationException.class,
                () -> passwordResetService.resetPassword(request)
        );

        assertThat(ex.getErrorCase())
                .isEqualTo(AuthErrorCase.PASSWORD_RESET_TOKEN_INVALID);
    }
}

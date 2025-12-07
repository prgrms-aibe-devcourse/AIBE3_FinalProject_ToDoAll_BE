package com.server.auth.service;

import com.server.auth.domain.EmailVerificationToken;
import com.server.auth.dto.EmailAuthSendRequestDto;
import com.server.auth.exception.AuthErrorCase;
import com.server.auth.repository.EmailVerificationTokenRepository;
import com.server.global.exception.ApplicationException;
import com.server.user.exception.UserErrorCase;
import com.server.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailAuthServiceTest {

    @Mock
    EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    JavaMailSender mailSender;

    @InjectMocks
    EmailAuthService emailAuthService;

    @Test
    @DisplayName("개인 이메일 도메인")
    void sendAuthEmail_personalDomain_throws() {
        EmailAuthSendRequestDto request =
                EmailAuthSendRequestDto.of("test@gmail.com");

        ApplicationException ex = assertThrows(ApplicationException.class,
                () -> emailAuthService.sendAuthEmail(request));

        assertThat(ex.getErrorCase()).isEqualTo(AuthErrorCase.EMAIL_NOT_ALLOWED);
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("이미 가입된 회사 이메일")
    void sendAuthEmail_alreadySignedUp_throws() {
        String email = "test@company.com";

        EmailAuthSendRequestDto request = EmailAuthSendRequestDto.of(email);

        given(userRepository.existsByEmail(email))
                .willReturn(true);

        ApplicationException ex = assertThrows(
                ApplicationException.class,
                () -> emailAuthService.sendAuthEmail(request)
        );

        assertThat(ex.getErrorCase()).isEqualTo(UserErrorCase.USER_ALREADY_EXISTS);
    }


    @Test
    @DisplayName("isVerifiedEmail - 대소문자/공백 무시하고 조회")
    void isVerifiedEmail_normalization() {
        String email = "  Test@Company.com  ";

        given(emailVerificationTokenRepository.existsByEmailAndVerifiedAtIsNotNull("test@company.com"))
                .willReturn(true);

        ReflectionTestUtils.setField(emailAuthService, "authBaseUrl", "https://test.com/auth");
        ReflectionTestUtils.setField(emailAuthService, "expiryMinutes", 30);

        boolean result = emailAuthService.isVerifiedEmail(email);

        assertThat(result).isTrue();
        verify(emailVerificationTokenRepository)
                .existsByEmailAndVerifiedAtIsNotNull("test@company.com");
    }

    @Test
    @DisplayName("최근 5분 이내에 이미 인증 메일이 발송됨")
    void sendAuthEmail_recentTokenExists_throws() {
        String email = "test@company.com";   // 이쪽도 회사 도메인으로

        EmailAuthSendRequestDto request = EmailAuthSendRequestDto.of(email);

        given(userRepository.existsByEmail(email)).willReturn(false);

        EmailVerificationToken token = EmailVerificationToken.createToken(email, 30);
        given(emailVerificationTokenRepository.findFirstByEmailAndCreatedAtAfter(
                eq(email),
                any(LocalDateTime.class)
        )).willReturn(Optional.of(token));

        ApplicationException ex = assertThrows(
                ApplicationException.class,
                () -> emailAuthService.sendAuthEmail(request)
        );

        assertThat(ex.getErrorCase()).isEqualTo(AuthErrorCase.EMAIL_AUTH_ALREADY_SENT);
    }

}

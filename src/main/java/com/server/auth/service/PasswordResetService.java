package com.server.auth.service;

import com.server.auth.dto.PasswordResetConfirmRequestDto;
import com.server.auth.dto.PasswordResetRequestDto;
import com.server.auth.dto.PasswordResetResponseDto;
import com.server.auth.exception.AuthErrorCase;
import com.server.auth.repository.PasswordResetTokenRepository;
import com.server.global.exception.ApplicationException;
import com.server.user.domain.PasswordResetToken;
import com.server.user.domain.User;
import com.server.user.repository.UserRepository;
import com.server.user.util.PasswordValidator;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;

    @Value("${app.password-reset.expiry-minutes:30}")
    private int expiryMinutes;

    @Value("${app.password-reset.frontend-url}")
    private String frontendResetUrl;

    // 비밀번호 재설정 이메일 발송

    @Transactional
    public PasswordResetResponseDto sendPasswordResetEmail(PasswordResetRequestDto request) {
        String email = request.email();

        // 1) 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    log.info("존재하지 않는 이메일로 비밀번호 재설정이 요청되었습니다.");
                    return null; // 조용히 null 반환
                });

        if (user == null) {
            // 보안을 위해 성공 응답
            return PasswordResetResponseDto.ofEmailSent();
        }

        // 2) 기존 토큰 무효화
        tokenRepository.findLatestUnusedToken(
                        user.getId(),
                        LocalDateTime.now()
                ).ifPresent(oldToken -> {
                    oldToken.markAsUsed();
                    log.info("이전 비밀번호 재설정 토큰이 무효화되었습니다.");
                });

        // 3) 새 토큰 생성
        String plainToken = generateSecureToken();
        String tokenHash = hashToken(plainToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expiryMinutes);

        PasswordResetToken token = PasswordResetToken.of(user, tokenHash, expiresAt);
        tokenRepository.save(token);

        // 4) 이메일 발송
        String resetLink = frontendResetUrl + "?token=" + plainToken;
        sendResetEmail(user.getEmail(), resetLink);

        log.info("비밀번호 재설정 이메일이 발송되었습니다.");

        return PasswordResetResponseDto.ofEmailSent();
    }

    // 비밀번호 재설정 실행

    @Transactional
    public PasswordResetResponseDto resetPassword(PasswordResetConfirmRequestDto request) {
        String plainToken = request.token();
        String newPassword = request.newPassword();

        // 1) 토큰 조회
        String tokenHash = hashToken(plainToken);
        PasswordResetToken token = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> {
                    log.warn("비밀번호 재설정 실패 - 유효하지 않은 토큰");
                    return ApplicationException.from(AuthErrorCase.PASSWORD_RESET_TOKEN_INVALID);
                });

        // 2) 토큰 검증
        if (token.isUsed()) {
            log.warn("비밀번호 재설정 실패");
            throw ApplicationException.from(AuthErrorCase.PASSWORD_RESET_TOKEN_ALREADY_USED);
        }

        if (token.isExpired()) {
            log.warn("비밀번호 재설정 실패");
            throw ApplicationException.from(AuthErrorCase.PASSWORD_RESET_TOKEN_EXPIRED);
        }

        // 3) 사용자 조회
        User user = token.getUser();

        // 4) 비밀번호 정책 검증
        passwordValidator.validateForSignup(newPassword, user.getEmail());

        // 5) 필드 주입된 passwordEncoder 사용
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.changePassword(encodedPassword);

        // 6) 토큰 사용 처리
        token.markAsUsed();

        log.info("비밀번호 변경 완료");

        return PasswordResetResponseDto.ofPasswordChanged();
    }


    // SecureRandom 토큰 생성

    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        random.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    //  SHA-256 해시로 변환

    private String hashToken(String plainToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainToken.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("토큰 해싱 실패", e);
            throw new RuntimeException("토큰 해싱 중 오류 발생", e);
        }
    }

    // 이메일 발송

    private void sendResetEmail(String toEmail, String resetLink) {
        try {
            String htmlContent = loadPasswordResetTemplate(toEmail, resetLink);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("[jobda] 비밀번호 재설정 안내");
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("비밀번호 재설정 이메일 발송 성공");
        } catch (Exception e) {
            log.error("비밀번호 재설정 이메일 발송 실패");
            throw ApplicationException.from(AuthErrorCase.EMAIL_SEND_FAILED);
        }
    }

    // 이메일 템플릿 로딩

    private String loadPasswordResetTemplate(String email, String resetLink) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/email/password-reset.html");

            try (InputStream is = resource.getInputStream()) {
                String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                html = html.replace("{{email}}", email);
                html = html.replace("{{verificationUrl}}", resetLink);
                html = html.replace("{{expiryMinutes}}", String.valueOf(expiryMinutes));
                html = html.replace("{{year}}", String.valueOf(Year.now().getValue()));

                return html;
            }
        } catch (Exception e) {
            log.error("템플릿 로드 실패", e);
            throw new RuntimeException("템플릿 로드 실패", e);
        }
    }
}
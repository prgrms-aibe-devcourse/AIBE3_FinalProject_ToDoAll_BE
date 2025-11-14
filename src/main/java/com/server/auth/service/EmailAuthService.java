package com.server.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.server.auth.domain.EmailVerificationToken;
import com.server.auth.dto.EmailAuthSendRequestDto;
import com.server.auth.dto.EmailAuthCompleteResponseDto;
import com.server.auth.exception.AuthErrorCase;
import com.server.auth.repository.EmailVerificationTokenRepository;
import com.server.global.exception.ApplicationException;
import com.server.user.exception.UserErrorCase;
import com.server.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Year;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailAuthService {

    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @Value("${app.email.auth-expiry-minutes:30}")
    private int expiryMinutes;

    @Value("${app.email.auth-base-url}")
    private String authBaseUrl;

    //회사 이메일 인증 메일 발송

    @Transactional
    public void sendAuthEmail(EmailAuthSendRequestDto request) {

        String email = request.getEmail();

        // 1) 이미 가입된 이메일인지 확인
        if (userRepository.existsByEmail(email)) {
            // 이미 가입된 회사 이메일이라면, 다시 가입용 인증 요청은 막기
            throw ApplicationException.from(UserErrorCase.USER_ALREADY_EXISTS);
        }

        // 2) 새 토큰 생성 (이메일 + 만료시간)
        EmailVerificationToken token = EmailVerificationToken.createToken(email, expiryMinutes);

        // 3) 저장
        emailVerificationTokenRepository.save(token);

        // 4) 인증 링크 생성
        String verificationLink = UriComponentsBuilder
                .fromHttpUrl(authBaseUrl)
                .queryParam("token", token.getToken())
                .toUriString();

        // 5) HTML 템플릿 로딩 + 변수 치환
        String htmlContent = loadSignupVerificationTemplate(verificationLink);

        // 6) 실제 이메일 전송
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(email);
            helper.setSubject("[jobda] 회원가입 이메일 인증 안내");
            helper.setText(htmlContent, true); // true = HTML 모드

            mailSender.send(message);
        } catch (Exception e) {
            // 메일 전송에 실패하면 서버 내부 오류로 래핑
            throw ApplicationException.from(AuthErrorCase.AUTH_INTERNAL_ERROR);
        }
    }


    //이메일 인증 완료 처리

    @Transactional
    public EmailAuthCompleteResponseDto completeAuth(String tokenValue) {

        // 1) 토큰 조회
        EmailVerificationToken token = emailVerificationTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> ApplicationException.from(AuthErrorCase.EMAIL_AUTH_TOKEN_INVALID));

        // 2) 이미 사용된 토큰인지 확인
        if (token.isVerified()) {
            throw ApplicationException.from(AuthErrorCase.EMAIL_AUTH_ALREADY_VERIFIED);
        }

        // 3) 만료 여부 확인
        if (token.isExpired()) {
            throw ApplicationException.from(AuthErrorCase.EMAIL_AUTH_TOKEN_EXPIRED);
        }

        // 4) 인증 완료 처리
        token.markAsVerified();

        // 5) 해당 이메일 User 찾아서 상태 변경
        userRepository.findByEmail(token.getEmail())
                .ifPresent(user -> user.markEmailVerified());

        // 6) 응답 DTO로 변환
        return EmailAuthCompleteResponseDto.from(token);
    }

     //회원가입 이메일 인증 HTML 템플릿 로드

    private String loadSignupVerificationTemplate(String verificationUrl) {
        try {
            // 1) classpath에서 HTML 템플릿 파일 읽기
            ClassPathResource resource =
                    new ClassPathResource("templates/email/signup-verification.html");

            try (InputStream is = resource.getInputStream()) {
                String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                html = html.replace("{{verificationUrl}}", verificationUrl);
                html = html.replace("{{year}}", String.valueOf(Year.now().getValue()));
                html = html.replace("{{expiryMinutes}}", String.valueOf(expiryMinutes));


                return html;
            }
        } catch (Exception e) {
            return "아래 링크를 클릭하여 이메일 인증을 완료해주세요.\n" + verificationUrl;
        }
    }
}

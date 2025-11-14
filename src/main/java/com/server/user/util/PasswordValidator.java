package com.server.user.util;

import com.server.global.exception.ApplicationException;
import com.server.user.exception.UserErrorCase;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 비밀번호 정책 검증 유틸리티

 검증 규칙:
 1. 영문 포함 필수
 2. 숫자 포함 필수
 3. 최소 8자 이상
 4. 이메일 앞부분 불포함 (3자 이상)
 5. 금지어 불포함 (password, admin)
 */

@Component
public class PasswordValidator {

    private static final int MIN_LENGTH = 8;
    private static final int MIN_PII_LENGTH = 3;
    private static final List<String> DEFAULT_BANNED = List.of("password", "admin");

    // 회원가입 시 비밀번호 검증
    public void validateForSignup(String password, String email) {
        validateBasicPolicy(password);
        validateWithPiiSources(password, email);
    }

    // 기본 정책: 영문 + 숫자 + 8자 이상
    private void validateBasicPolicy(String password) {
        boolean hasEnglish = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasMinLength = password.length() >= MIN_LENGTH;

        if (!hasEnglish || !hasDigit || !hasMinLength) {
            throw ApplicationException.from(UserErrorCase.USER_VALIDATION_FAILED);
        }
    }

    // 개인정보 + 금지어 통합 검사
    private void validateWithPiiSources(String password, String email) {
        String lowerPwd = password.toLowerCase();

        // 1) 이메일 앞부분 검사
        if (email != null) {
            String trimmedEmail = email.trim();
            int atIndex = trimmedEmail.indexOf("@");

            if (atIndex > 0) {
                String localPart = trimmedEmail.substring(0, atIndex).toLowerCase().trim();

                if (localPart.length() >= MIN_PII_LENGTH && lowerPwd.contains(localPart)) {
                    throw ApplicationException.from(UserErrorCase.PASSWORD_EQUALS_EMAIL_ID);
                }
            }
        }

        // 2) 금지어
        boolean containsForbidden = DEFAULT_BANNED.stream()
                .anyMatch(lowerPwd::contains);

        if (containsForbidden) {
            throw ApplicationException.from(UserErrorCase.USER_VALIDATION_FAILED);
        }
    }
}

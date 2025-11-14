package com.server.user.service;

import com.server.global.exception.ApplicationException;
import com.server.user.domain.User;
import com.server.user.dto.UserSignupRequestDto;
import com.server.user.dto.UserSignupResponseDto;
import com.server.user.exception.UserErrorCase;
import com.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserSignupResponseDto signup(UserSignupRequestDto request) {
        // 1) 이메일 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            throw ApplicationException.from(UserErrorCase.USER_ALREADY_EXISTS);
        }

        // 2) 비밀번호 & 비밀번호 확인 불일치
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw ApplicationException.from(UserErrorCase.PASSWORD_MISMATCH);
        }

        // 3) 비밀번호 정책 검사
        validatePasswordPolicy(
                request.getPassword(),
                request.getEmail()
        );

        // 4) 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 5) 엔티티 생성
        User newUser = User.createForSignup(
                request.getEmail(),
                encodedPassword,
                request.getName(),
                request.getNickname(),
                request.getCompanyName(),
                request.getPosition()
        );

        // 6) 저장
        User saved = userRepository.save(newUser);

        // 7) 응답 DTO
        return UserSignupResponseDto.from(saved);
    }

    // === 비밀번호 정책 검사 === //
    private void validatePasswordPolicy(String password, String email) {

        // 1) 영어 포함 검사
        boolean hasEnglish = password.chars().anyMatch(Character::isLetter);

        // 2) 숫자 포함 검사
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);

        // 3) 길이 검사
        boolean hasMinLength = password.length() >= 8;

        if (!hasEnglish || !hasDigit || !hasMinLength) {
            throw ApplicationException.from(UserErrorCase.USER_VALIDATION_FAILED);
        }

        // 4) 개인정보/금지어 포함 검사
        String lowerPwd = password.toLowerCase();
        List<String> needles = new ArrayList<>();

        // 기본 금지어
        needles.add("password");;
        needles.add("admin");

        // 이메일
        if (email != null) {
            String localPart = email.split("@")[0].toLowerCase();
            if (localPart.length() >= 3) needles.add(localPart);
        }

        boolean containsPII = needles.stream()
                .anyMatch(word -> lowerPwd.contains(word.trim()));

        if (containsPII) {
            throw ApplicationException.from(UserErrorCase.USER_VALIDATION_FAILED);
        }
    }







}

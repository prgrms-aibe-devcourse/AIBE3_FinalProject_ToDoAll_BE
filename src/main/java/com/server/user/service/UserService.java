package com.server.user.service;

import com.server.global.exception.ApplicationException;
import com.server.user.domain.User;
import com.server.user.dto.UserSignupRequestDto;
import com.server.user.dto.UserSignupResponseDto;
import com.server.user.exception.UserErrorCase;
import com.server.user.repository.UserRepository;
import com.server.user.util.PasswordValidator;
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
    private final PasswordValidator passwordValidator;

    @Transactional
    public UserSignupResponseDto signup(UserSignupRequestDto request) {
        // 1) 이메일 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            throw ApplicationException.from(UserErrorCase.USER_ALREADY_EXISTS);
        }

        // 2) 비밀번호 & 비밀번호 확인 불일치
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw ApplicationException.from(UserErrorCase.PASSWORD_CONFIRM_MISMATCH);
        }

        // 3) 비밀번호 정책 검사
        passwordValidator.validateForSignup(
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

}

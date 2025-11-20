package com.server.user.service;

import com.server.auth.exception.AuthErrorCase;
import com.server.auth.service.EmailAuthService;
import com.server.global.exception.ApplicationException;
import com.server.user.domain.User;
import com.server.user.dto.UserProfileResponseDto;
import com.server.user.dto.UserProfileUpdateRequestDto;
import com.server.user.dto.UserSignupRequestDto;
import com.server.user.dto.UserSignupResponseDto;
import com.server.user.exception.UserErrorCase;
import com.server.user.repository.UserRepository;
import com.server.user.util.PasswordValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;
    private final EmailAuthService emailAuthService;    // 이메일 인증 검증


    //회원가입
    @Transactional
    public UserSignupResponseDto signup(UserSignupRequestDto request) {

        // 0) 이메일 인증 여부 확인 (이메일 + 토큰)
        if (!emailAuthService.isVerifiedEmail(request.getEmail())) {
            throw ApplicationException.from(AuthErrorCase.EMAIL_AUTH_REQUIRED);
        }

        // 1) 이메일 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            throw ApplicationException.from(UserErrorCase.USER_ALREADY_EXISTS);
        }

        // 2) 비밀번호 정책 검사
        passwordValidator.validateForSignup(
                request.getPassword(),
                request.getEmail()
        );

        // 3) 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 4) 유저 생성
        User newUser = User.createForSignup(
                request.getEmail(),
                encodedPassword,
                request.getName(),
                request.getNickname(),
                request.getCompanyName(),
                request.getPosition()
        );

        // 5) 저장
        User saved = userRepository.save(newUser);

        // 6) 응답 DTO
        return UserSignupResponseDto.from(saved);
    }

    // 마이페이지 - 내 정보 조회

    public UserProfileResponseDto getMyProfile(Long userId) {
        validateAuthenticated(userId); // 인증 여부 검증을 서비스에서 처리
        // 1) userId 로 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApplicationException.from(UserErrorCase.USER_NOT_FOUND));

        // 2) 엔티티 -> DTO 변환
        return UserProfileResponseDto.from(user);
    }

    // 마이페이지 - 내 정보 수정

    @Transactional
    public UserProfileResponseDto updateMyProfile(Long userId, UserProfileUpdateRequestDto request) {
        validateAuthenticated(userId); // 인증 여부 검증

        // 1) userId 로 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApplicationException.from(UserErrorCase.USER_NOT_FOUND));


        // 3) 엔티티의 프로필 업데이트 메서드 호출
        user.updateProfile(
                request.getName(),
                request.getNickname(),
                request.getPosition(),
                request.getPhoneNumber(),
                request.getBirthDate(),
                request.getGender()
        );

        // 4) 변경된 user 엔티티를 응답 DTO로 변환해서 반환
        return UserProfileResponseDto.from(user);
    }


    //비밀번호 변경

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        validateAuthenticated(userId); // 인증 여부 검증 (컨트롤러에서 제거됨)


        // 1) user 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApplicationException.from(UserErrorCase.USER_NOT_FOUND));

        // 2) 현재 비밀번호 검증
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw ApplicationException.from(UserErrorCase.INVALID_PASSWORD);
        }

        // 3) 새로운 비밀번호 정책 검사
        passwordValidator.validateForSignup(newPassword, user.getEmail());

        // 4) 이전 비번과 동일한지
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw ApplicationException.from(UserErrorCase.PASSWORD_SAME_AS_OLD);
        }

        // 5) 새 비밀번호 암호화
        String encodedNewPassword = passwordEncoder.encode(newPassword);

        // 6) 엔티티에서 변경
        user.changePassword(encodedNewPassword);
    }

    // 인증 여부를 공통으로 검증하는 private 메서드
    private void validateAuthenticated(Long userId) {
        if (userId == null) {
            // 로그인 정보가 없으면 UNAUTHORIZED 에러 반환
            throw ApplicationException.from(UserErrorCase.UNAUTHORIZED);
        }
    }

}

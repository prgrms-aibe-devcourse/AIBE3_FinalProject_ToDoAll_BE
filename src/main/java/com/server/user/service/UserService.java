package com.server.user.service;

import com.server.auth.exception.AuthErrorCase;
import com.server.auth.service.EmailAuthService;
import com.server.global.exception.ApplicationException;
import com.server.s3.domain.Partition;
import com.server.s3.service.PresignedUrlProvider;
import com.server.s3.service.S3Uploader;
import com.server.user.config.UserProperties;
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
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;
    private final EmailAuthService emailAuthService;    // 이메일 인증 검증
    private final UserProperties userProperties;        // 기본 프로필 이미지 설정값
    private final S3Uploader s3Uploader;                  // S3 업로드
    private final PresignedUrlProvider presignedUrlProvider; // S3 파일 URL 생성





    //회원가입
    @Transactional
    public UserSignupResponseDto signup(UserSignupRequestDto request) {

        // 0) 이메일 인증 토큰 검증 및 사용 처리
        String verifiedEmail = emailAuthService.validateAndUseToken(request.getToken());

        // 0-1) 토큰에서 나온 이메일과 요청 이메일 일치 확인
        if (!verifiedEmail.equalsIgnoreCase(request.getEmail())) {
            throw ApplicationException.from(AuthErrorCase.EMAIL_AUTH_TOKEN_INVALID);
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
        validateAuthenticated(userId);
        // 1) userId 로 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApplicationException.from(UserErrorCase.USER_NOT_FOUND));

        // 2) 엔티티 -> DTO 변환 (기본 프로필 URL 적용)
        String profileImageUrl = resolveProfileImageUrl(user);
        return UserProfileResponseDto.from(user, profileImageUrl
        );
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

        String profileImageUrl = resolveProfileImageUrl(user);
        return UserProfileResponseDto.from(
                user,
                profileImageUrl
        );
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

    //  마이페이지 - 프로필 이미지 변경(S3)
    @Transactional
    public UserProfileResponseDto updateProfileImage(Long userId, MultipartFile file) {
        validateAuthenticated(userId); // 로그인 여부 체크

        if (file == null || file.isEmpty()) {
            // 파일이 없거나 비어 있으면 예외
            throw ApplicationException.from(UserErrorCase.INVALID_PROFILE_IMAGE);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApplicationException.from(UserErrorCase.USER_NOT_FOUND));

        String fileKey;

        // 기존에 프로필이 있었으면 → updateFile로 교체 (기존 파일 삭제 포함)
        if (user.getProfileUrl() != null && !user.getProfileUrl().isBlank()) {
            fileKey = s3Uploader.updateFile(file, user.getProfileUrl());
        } else {
            // 첫 업로드면 → uploadFile
            fileKey = s3Uploader.uploadFile(
                    file,
                    Partition.USER,
                    String.valueOf(userId),
                    "profile"
            );
        }

        // 엔티티에 새로운 파일키 저장
        user.changeProfileImage(fileKey);

        String profileImageUrl = resolveProfileImageUrl(user);

        // 변경된 정보로 응답 DTO 생성 (기본 이미지 URL 포함)
        return UserProfileResponseDto.from(
                user, profileImageUrl
        );
    }


    // ================== 프로필 이미지 URL 조회 (302 redirect용) ==================
    public String getProfileImageUrl(Long userId) {
        validateAuthenticated(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApplicationException.from(UserErrorCase.USER_NOT_FOUND));

        return resolveProfileImageUrl(user);
    }

    // 공통 유틸 메서드

    private String resolveProfileImageUrl(User user) {
        String profileKey = user.getProfileUrl();

        if (profileKey == null || profileKey.isBlank()) {
            // S3에 업로드된 이미지가 없으면 기본 프로필 jpg 사용
            return userProperties.getDefaultProfileImageUrl(); // ex) "/images/default-profile.jpg" 또는 절대 URL
        }

        // S3 fileKey 기준으로 presigned URL 생성
        return presignedUrlProvider.createPresignedGetUrl(profileKey);
    }


    // 인증 여부를 공통으로 검증하는 private 메서드
    private void validateAuthenticated(Long userId) {
        if (userId == null) {
            // 로그인 정보가 없으면 UNAUTHORIZED 에러 반환
            throw ApplicationException.from(UserErrorCase.UNAUTHORIZED);
        }
    }

}

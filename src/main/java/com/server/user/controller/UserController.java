package com.server.user.controller;


import com.server.global.response.CommonResponse;
import com.server.user.dto.*;
import com.server.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "UserController", description = "API 유저 컨트롤러")
public class UserController {

    private final UserService userService;

    //회원가입
    @PostMapping
    public ResponseEntity<CommonResponse<UserSignupResponseDto>> signup(
            @Valid @RequestBody UserSignupRequestDto request
    ) {
        UserSignupResponseDto response = userService.signup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CommonResponse.success(response));
    }

     //로그인 후 마이페이지에서 비밀번호 변경

    @PatchMapping("/me/password")
    public CommonResponse<String> changePassword(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ChangePasswordRequestDto request
    ) {
        // 혹시 인증 없이 들어온 경우 방어 코드

        userService.changePassword(
                userId,
                request.currentPassword(),
                request.newPassword()
        );

        return CommonResponse.success("비밀번호가 변경되었습니다.");
    }

    // 마이페이지 - 내 정보 조회
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.")
    @GetMapping("/me")
    public CommonResponse<UserProfileResponseDto> getMyProfile(
            @AuthenticationPrincipal Long userId
    ) {
        log.info("getMe principal: {}", userId);

        // 1) 서비스 호출해서 프로필 조회
        UserProfileResponseDto profile = userService.getMyProfile(userId);

        // 2) 공통 응답 래퍼로 감싸서 반환
        return CommonResponse.success(profile);
    }

    // 마이페이지 - 내 정보 수정
    @Operation(summary = "내 정보 수정", description = "현재 로그인한 사용자의 정보를 수정합니다.")
    @PatchMapping("/me")
    public CommonResponse<UserProfileResponseDto> updateMyProfile(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserProfileUpdateRequestDto request
    ) {

        // 1) 서비스 호출해서 프로필 수정
        UserProfileResponseDto updated = userService.updateMyProfile(userId, request);

        // 2) 수정된 결과를 응답
        return CommonResponse.success(updated);
    }

    //  마이페이지 - 프로필 이미지 업로드 (S3에 저장 후, 변경된 프로필 응답)
    @Operation(summary = "내 프로필 이미지 변경")
    @PatchMapping(value = "/me/profile-image", consumes = "multipart/form-data")
    public CommonResponse<UserProfileResponseDto> updateMyProfileImage(
            @AuthenticationPrincipal Long userId,
            @RequestPart("file") MultipartFile file
    ) {
        log.info("updateMyProfileImage userId: {}, fileName: {}", userId, file.getOriginalFilename());

        UserProfileResponseDto updated = userService.updateProfileImage(userId, file);
        return CommonResponse.success(updated);
    }
    //  프로필 이미지 삭제 (기본 이미지로 되돌리기)

    @Operation(summary = "프로필 이미지 삭제",
            description = "프로필 이미지 삭제 ->기본 이미지")
    @DeleteMapping("/me/profile-image")
    public CommonResponse<UserProfileResponseDto> deleteMyProfileImage(
            @AuthenticationPrincipal Long userId
    ) {
        UserProfileResponseDto updated = userService.removeProfileImage(userId);
        return CommonResponse.success(updated);
    }

    @GetMapping("/domain")
    @Operation(summary = "같은 회사 유저 조회", description = "같은 회사인 유저들을 전체 조회")
    public CommonResponse<List<UsersByEmailDomainResponseDto>> getUsersByDomain()
    {
        List<UsersByEmailDomainResponseDto> response = userService.getUsersByEmailDomain();
        return CommonResponse.success(response);
    }

    @Operation(summary = "아이디로 조회", description = "아이디로 프로필을 조회합니다.")
    @GetMapping("/{userId}")
    public CommonResponse<UserProfileSearchResponseDto> getUserProfile(
            @PathVariable Long userId
    ) {
        log.info("getMe principal: {}", userId);

        // 1) 서비스 호출해서 프로필 조회
        UserProfileSearchResponseDto profile = userService.getUserProfile(userId);

        // 2) 공통 응답 래퍼로 감싸서 반환
        return CommonResponse.success(profile);
    }

}

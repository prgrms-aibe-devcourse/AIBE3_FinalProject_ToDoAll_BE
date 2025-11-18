package com.server.user.controller;


import com.server.global.config.security.jwt.JwtAuthentication;
import com.server.global.exception.ApplicationException;
import com.server.global.response.CommonResponse;
import com.server.user.dto.ChangePasswordRequestDto;
import com.server.user.dto.UserSignupRequestDto;
import com.server.user.dto.UserSignupResponseDto;
import com.server.user.exception.UserErrorCase;
import com.server.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
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
        if (userId == null) {
            throw ApplicationException.from(UserErrorCase.UNAUTHORIZED);
        }

        userService.changePassword(
                userId,
                request.currentPassword(),
                request.newPassword()
        );

        return CommonResponse.success("비밀번호가 변경되었습니다.");
    }

}

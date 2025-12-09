package com.server.auth.controller;

import com.server.auth.dto.EmailAuthSendRequestDto;
import com.server.auth.dto.EmailAuthCompleteResponseDto;
import com.server.auth.service.EmailAuthService;
import com.server.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/email-verifications")
public class EmailAuthController {

    private final EmailAuthService emailAuthService;


    //회사 이메일 인증 메일 발송
    @Operation(
            summary = "회사 이메일 인증 메일 발송",
            description = "회사 이메일 주소로 회원가입용 인증 메일을 발송합니다. ")
    @PostMapping
    public CommonResponse<Void> sendEmailAuth(
            @Valid @RequestBody EmailAuthSendRequestDto request
    ) {
        emailAuthService.sendAuthEmail(request);
        return CommonResponse.success(null);
    }


    //이메일 인증 완료
    @Operation(
            summary = "이메일 인증 완료",
            description = """
                    이메일로 전송된 인증 링크를 통해 전달된 토큰을 검증하고,
                    해당 이메일의 인증 상태를 완료 처리합니다.""")
    @GetMapping("/complete")
    public CommonResponse<EmailAuthCompleteResponseDto> completeEmailAuth(
            @RequestParam("token") String token
    ) {
        EmailAuthCompleteResponseDto response = emailAuthService.completeAuth(token);
        return CommonResponse.success(response);
    }
}


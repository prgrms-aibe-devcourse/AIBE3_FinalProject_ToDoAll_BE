package com.server.auth.controller;

import com.server.auth.dto.EmailAuthSendRequestDto;
import com.server.auth.dto.EmailAuthCompleteResponseDto;
import com.server.auth.service.EmailAuthService;
import com.server.global.response.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/email-verifications")
public class EmailAuthController {

    private final EmailAuthService emailAuthService;


    //회사 이메일 인증 메일 발송

    @PostMapping
    public CommonResponse<Void> sendEmailAuth(
            @Valid @RequestBody EmailAuthSendRequestDto request
    ) {
        emailAuthService.sendAuthEmail(request);
        return CommonResponse.success(null);
    }


    //이메일 인증 완료

    @GetMapping("/{token}")
    public CommonResponse<EmailAuthCompleteResponseDto> completeEmailAuth(
            @RequestParam("token") String token
    ) {
        EmailAuthCompleteResponseDto response = emailAuthService.completeAuth(token);
        return CommonResponse.success(response);
    }
}


package com.server.auth.dto;

import lombok.Builder;

@Builder
public record PasswordResetResponseDto(
        String message
) {
    // 이메일 발송 성공
    public static PasswordResetResponseDto ofEmailSent() {
        return PasswordResetResponseDto.builder()
                .message("비밀번호 재설정 링크가 이메일로 발송되었습니다. (유효시간: 30분)")
                .build();
    }

    // 비밀번호 변경 성공
    public static PasswordResetResponseDto ofPasswordChanged() {
        return PasswordResetResponseDto.builder()
                .message("비밀번호가 성공적으로 변경되었습니다. 새 비밀번호로 로그인해주세요.")
                .build();
    }
}

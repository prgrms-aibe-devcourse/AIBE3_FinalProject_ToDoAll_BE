package com.server.auth.dto;

import com.server.auth.domain.EmailVerificationToken;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class EmailAuthCompleteResponseDto {

    private String email;
    private LocalDateTime verifiedAt;

    private EmailAuthCompleteResponseDto(String email, LocalDateTime verifiedAt) {
        this.email = email;
        this.verifiedAt = verifiedAt;
    }

    public static EmailAuthCompleteResponseDto from(EmailVerificationToken token) {
        return new EmailAuthCompleteResponseDto(
                token.getEmail(),
                token.getVerifiedAt()
        );
    }
}

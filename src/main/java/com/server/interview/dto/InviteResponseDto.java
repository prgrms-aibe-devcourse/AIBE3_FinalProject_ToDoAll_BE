package com.server.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InviteResponseDto {
    private Long interviewId;
    private String email;
    private String token;
}

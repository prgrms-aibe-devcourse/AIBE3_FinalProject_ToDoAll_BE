package com.server.interview.dto;

public record InterviewRequestDto(
        // 예시일 뿐입니다. 아마 1대다 면접 생성 같은거 하려면 더 필요할거같아요.
        Long jdId,
        Long userId
) {
}

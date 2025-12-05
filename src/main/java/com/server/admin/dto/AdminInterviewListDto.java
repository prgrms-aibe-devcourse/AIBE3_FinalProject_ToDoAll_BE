package com.server.admin.dto;

import com.server.interview.domain.Interview;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AdminInterviewListDto(
        Long id,
        String jdTitle,
        String candidateName,
        LocalDateTime scheduledAt,
        String status,
        boolean deleted
) {
    public static AdminInterviewListDto from(Interview i) {
        return AdminInterviewListDto.builder()
                .id(i.getId())
                .jdTitle(i.getJobDescription().getTitle())
                .candidateName(i.getResume().getName())
                .scheduledAt(i.getScheduledAt())
                .status(i.getStatus().name())
                .deleted(i.isDeleted())
                .build();
    }
}

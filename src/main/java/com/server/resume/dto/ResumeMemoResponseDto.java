package com.server.resume.dto;

import com.server.resume.domain.Resume;
import com.server.resume.domain.ResumeStatus;

public record ResumeMemoResponseDto (
        Long id,
        String memo
) {
    public static ResumeMemoResponseDto from(Resume resume) {
        return new ResumeMemoResponseDto(resume.getId(), resume.getMemo());
    }
}

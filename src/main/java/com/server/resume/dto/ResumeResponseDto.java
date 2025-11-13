package com.server.resume.dto;

import com.server.jd.domain.JobDescription;
import com.server.resume.domain.ResumeStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public record ResumeResponseDto(
        Long id,
        Long jobId,
        String jobTitle,
        String name,
        String gender,
        LocalDate birthDate,
        String email,
        String phone,
        String address,
        String detailAddress,
        List<ResumeEducationResponseDto> education,
        List<ResumeExperienceResponseDto> experience,
        List<ResumeSkillResponseDto> skills,
        List<ResumeActivityResponseDto> activities,
        List<ResumeCertificationResponseDto> certifications,
        String resumeFileUrl,
        String portfolioFileUrl,
        ResumeStatus status
) {
    public static ResumeResponseDto fromEntity(
            com.server.resume.domain.Resume resume,
            List<String> education,
            List<String> experience,
            List<String> skills,
            List<String> activities,
            List<String> certifications
    ) {
        Long jobId = null;
        String jobTitle = null;
        if (resume.getJobDescription() != null) {
            jobId = resume.getJobDescription().getId();
            jobTitle = resume.getJobDescription().getTitle();
        }

        return new ResumeResponseDto(
                resume.getId(),
                jobId,
                jobTitle,
                resume.getName(),
                resume.getGender(),
                resume.getBirthDate(),
                resume.getEmail(),
                resume.getPhone(),
                resume.getAddress(),
                resume.getDetailAddress(),
                resume.getEducations().stream()
                        .map(ResumeEducationResponseDto::fromEntity)
                        .collect(Collectors.toList()),
                resume.getExperiences().stream()
                        .map(ResumeExperienceResponseDto::fromEntity)
                        .collect(Collectors.toList()),
                resume.getSkills().stream()
                        .map(ResumeSkillResponseDto::fromEntity)
                        .collect(Collectors.toList()),
                resume.getActivities().stream()
                        .map(ResumeActivityResponseDto::fromEntity)
                        .collect(Collectors.toList()),
                resume.getCertifications().stream()
                                .map(ResumeCertificationResponseDto::fromEntity)
                                        .collect(Collectors.toList()),
                resume.getResumeFileUrl(),
                resume.getPortfolioFileUrl(),
                resume.getStatus()
        );
    }
}

package com.server.resume.dto;

import com.server.jd.domain.JobDescription;
import java.time.LocalDate;
import java.util.List;

public record ResumeCreateRequestDto(
        String name,
        JobDescription jobDescription,
        String gender,
        LocalDate birthDate,
        String email,
        String phone,
        String address,
        String detailAddress,
        List<ResumeEducationRequestDto> education,
        List<ResumeExperienceRequestDto> experience,
        List<ResumeSkillRequestDto> skills,
        List<ResumeActivityRequestDto> activities,
        List<ResumeCertificationRequestDto> certifications,
        String resumeFileUrl,
        String portfolioFileUrl
) {}

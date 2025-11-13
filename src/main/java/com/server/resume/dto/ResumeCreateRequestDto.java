package com.server.resume.dto;

import com.server.jd.domain.JobDescription;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.List;

public record ResumeCreateRequestDto(
        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @NotBlank(message = "지원 직무는 필수입니다.")
        JobDescription jobDescription,

        @NotBlank(message = "성별은 필수입니다.")
        String gender,

        @NotBlank(message = "생년월일은 필수입니다.")
        LocalDate birthDate,

        @NotBlank(message = "이메일은 필수입니다.")
        String email,

        @NotBlank(message = "전화번호는 필수입니다.")
        String phone,

        @NotBlank(message = "주소는 필수입니다.")
        String address,

        @NotBlank(message = "상세 주소는 필수입니다.")
        String detailAddress,

        List<ResumeEducationRequestDto> education,
        List<ResumeExperienceRequestDto> experience,
        List<ResumeSkillRequestDto> skills,
        List<ResumeActivityRequestDto> activities,
        List<ResumeCertificationRequestDto> certifications,
        String resumeFileUrl,
        String portfolioFileUrl
) {}

package com.server.resume.dto;

import com.server.resume.domain.AttendanceType;
import com.server.resume.domain.EducationLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;


public record ResumeEducationRequestDto(

        @NotNull(message = "학력을 선택해주세요.")
        EducationLevel educationLevel,

        @NotBlank(message = "학교 이름을 입력해주세요.")
        String schoolName,

        String major,

        @NotNull(message = "졸업 여부를 입력해주세요.")
        Boolean isGraduated,

        @NotNull(message = "입학일을 입력해주세요.")
        LocalDate admissionDate,

        LocalDate graduationDate,

        @NotNull(message = "수업 형태를 입력해 주세요.")
        AttendanceType attendanceType,

        @Positive(message = "취득 학점은 양수여야 합니다.")
        double gpa,

        @Positive(message = "기준 학점은 양수여야 합니다.")
        double gpaScale
){}

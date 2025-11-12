package com.server.resume.dto;

import com.server.resume.domain.AttendanceType;
import com.server.resume.domain.EducationLevel;

import java.time.LocalDate;
import java.util.Date;

public record ResumeEducationRequestDto(
        EducationLevel educationLevel,
        String schoolName,
        String major,
        Boolean isGraduated,
        LocalDate admissionDate,
        LocalDate graduationDate,
        AttendanceType attendanceType,
        double gpa,
        double gpaScale
        ) { }

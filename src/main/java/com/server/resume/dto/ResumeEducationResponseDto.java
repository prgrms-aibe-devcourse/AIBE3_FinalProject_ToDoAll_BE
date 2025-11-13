package com.server.resume.dto;

import com.server.resume.domain.*;

import java.time.LocalDate;
import java.util.Optional;

public record ResumeEducationResponseDto(
        EducationLevel educationLevel,
        String schoolName,
        String major,
        Boolean isGraduated,
        LocalDate admissionDate,
        LocalDate graduationDate,
        AttendanceType attendanceType,
        double gpa,
        double gpaScale
) {
    public static ResumeEducationResponseDto fromEntity(ResumeEducation education) {

        return new ResumeEducationResponseDto(
                education.getEducationLevel(),
                education.getSchoolName(),
                education.getMajor(),
                education.getIsGraduated(),
                education.getAdmissionDate(),
                education.getGraduationDate(),
                education.getAttendanceType(),
                education.getGpa() != null ? education.getGpa() : 0.0,
                education.getGpaScale() != null ? education.getGpaScale() : 0.0
        );


    }
}

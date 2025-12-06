package com.server.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminResumeForm {
    private Long jobDescriptionId;
    private String name;
    private String gender;
    private String birthDate;
    private String email;
    private String phone;
    private String address;
    private String detailAddress;

    private String resumeFileUrl;
    private String portfolioFileUrl;

    // JSON 입력
    private String educationJson;
    private String experienceJson;
    private String skillsJson;
    private String activitiesJson;
    private String certificationsJson;
}

package com.server.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;

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

    public String toJson() {
        return """
    {
      "name": "%s",
      "jobDescriptionId": %d,
      "gender": "%s",
      "birthDate": "%s",
      "email": "%s",
      "phone": "%s",
      "address": "%s",
      "detailAddress": "%s",
      "resumeFileUrl": "%s",
      "portfolioFileUrl": "%s",
      "education": %s,
      "experience": %s,
      "skills": %s,
      "activities": %s,
      "certifications": %s
    }
    """.formatted(
                name, jobDescriptionId, gender, birthDate, email, phone, address,
                detailAddress, resumeFileUrl, portfolioFileUrl,
                defaultArray(educationJson),
                defaultArray(experienceJson),
                defaultArray(skillsJson),
                defaultArray(activitiesJson),
                defaultArray(certificationsJson)
        );
    }

    private String defaultArray(String json) {
        return StringUtils.hasText(json) ? json : "[]";
    }
}

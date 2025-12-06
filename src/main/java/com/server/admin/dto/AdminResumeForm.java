package com.server.admin.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.resume.domain.Resume;
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

    private static final ObjectMapper mapper = new ObjectMapper();

    private static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }

    private String defaultArray(String json) {
        return StringUtils.hasText(json) ? json : "[]";
    }

    public static AdminResumeForm from(Resume resume) {
        AdminResumeForm form = new AdminResumeForm();
        form.setJobDescriptionId(resume.getJobDescription().getId());
        form.setName(resume.getName());
        form.setGender(resume.getGender());
        form.setBirthDate(resume.getBirthDate().toString());
        form.setEmail(resume.getEmail());
        form.setPhone(resume.getPhone());
        form.setAddress(resume.getAddress());
        form.setDetailAddress(resume.getDetailAddress());
        form.setResumeFileUrl(resume.getResumeFileUrl());
        form.setPortfolioFileUrl(resume.getPortfolioFileUrl());

        // JSON 직렬화
        form.setSkillsJson(toJson(resume.getSkills()));
        form.setExperienceJson(toJson(resume.getExperiences()));
        form.setEducationJson(toJson(resume.getEducations()));
        form.setActivitiesJson(toJson(resume.getActivities()));
        form.setCertificationsJson(toJson(resume.getCertifications()));

        return form;
    }

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
                name, jobDescriptionId, gender, birthDate, email, phone, address, detailAddress,
                resumeFileUrl, portfolioFileUrl,
                defaultArray(educationJson),
                defaultArray(experienceJson),
                defaultArray(skillsJson),
                defaultArray(activitiesJson),
                defaultArray(certificationsJson)
        );
    }
}
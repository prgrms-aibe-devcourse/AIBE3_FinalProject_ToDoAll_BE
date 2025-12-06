package com.server.admin.dto;

import com.server.jd.domain.JobDescription;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminJdForm {

    private String title;
    private String department;
    private String workType;
    private String experience;
    private String education;
    private String salary;
    private String description;
    private String deadline;
    private String welfare;
    private String location;
    private String thumbnailUrl;
    private Long authorId;
    private String requiredSkills;
    private String preferredSkills;

    public static AdminJdForm from(JobDescription jd) {
        AdminJdForm form = new AdminJdForm();
        form.setTitle(jd.getTitle());
        form.setDepartment(jd.getDepartment());
        form.setWorkType(jd.getWorkType());
        form.setExperience(jd.getExperience());
        form.setEducation(jd.getEducation());
        form.setSalary(jd.getSalary());
        form.setDescription(jd.getDescription());
        form.setDeadline(jd.getDeadline() != null ? jd.getDeadline().toString() : null);
        form.setWelfare(jd.getWelfare());
        form.setLocation(jd.getLocation());
        form.setThumbnailUrl(jd.getThumbnailUrl());
        form.setAuthorId(jd.getAuthor().getId());
        form.setRequiredSkills(String.join(", ", jd.getRequiredSkillNames()));
        form.setPreferredSkills(String.join(", ", jd.getPreferredSkillNames()));
        return form;
    }
}

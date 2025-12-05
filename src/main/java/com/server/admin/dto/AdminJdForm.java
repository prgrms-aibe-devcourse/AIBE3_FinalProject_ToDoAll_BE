package com.server.admin.dto;

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
    private String benefits;
    private String location;
    private String thumbnailUrl;
    private Long authorId;
    private String requiredSkills;
    private String preferredSkills;
}

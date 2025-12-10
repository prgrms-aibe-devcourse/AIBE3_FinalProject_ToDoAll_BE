package com.server.jd.dto;

import com.server.jd.domain.JobDescription;

public record JobDescriptionOptionDto (Long jdId, String title){
    public static JobDescriptionOptionDto from(JobDescription jobDescription) {
        return new JobDescriptionOptionDto(
                jobDescription.getId(),
                jobDescription.getTitle()
        );
    }
}

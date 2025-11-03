package com.server.interview.dto;

import com.server.interview.domain.Interview;
import lombok.Getter;

@Getter
public class InterviewResponseDto {

    private final Long id;
    private final String status;

    public InterviewResponseDto(Interview interview) {
        this.id = interview.getId();
        this.status = interview.getStatus().name();
    }
}

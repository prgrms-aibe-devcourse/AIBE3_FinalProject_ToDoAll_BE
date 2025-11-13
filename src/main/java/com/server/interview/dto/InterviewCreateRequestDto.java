package com.server.interview.dto;

import java.time.LocalDateTime;
import java.util.List;

public record InterviewCreateRequestDto (
        Long jd_id,
        Long resume_id,
        List<Long> participant_ids,
        LocalDateTime scheduledAt
){
}

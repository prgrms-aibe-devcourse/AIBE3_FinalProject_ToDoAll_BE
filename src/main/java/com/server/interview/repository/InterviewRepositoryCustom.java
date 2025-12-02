package com.server.interview.repository;

import com.server.interview.dto.InterviewSummaryDto;

import java.util.List;

public interface InterviewRepositoryCustom {
    List<InterviewSummaryDto> searchInterviews(
            Long userId,
            Long jdId,
            String status,
            Long cursor,
            String sort,
            int limit
    );
}

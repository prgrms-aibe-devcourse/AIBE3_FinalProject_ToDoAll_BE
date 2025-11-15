package com.server.interview.repository;

import com.server.interview.domain.Interview;
import java.util.List;

public interface InterviewRepositoryCustom {
    List<Interview> searchInterviews(
            Long jdId,
            String status,
            Long cursor,
            String sort,
            int limit
    );
}

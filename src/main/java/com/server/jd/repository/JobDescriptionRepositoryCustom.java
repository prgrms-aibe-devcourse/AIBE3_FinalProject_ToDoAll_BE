package com.server.jd.repository;

import com.server.jd.dto.JobDescriptionInterviewOptionDto;

import java.util.List;

public interface JobDescriptionRepositoryCustom {
    List<JobDescriptionInterviewOptionDto> findJdListByInterviewParticipant(Long userId);
}

package com.server.interview.service;

import com.server.global.exception.ApplicationException;
import com.server.interview.domain.Interview;
import com.server.interview.exception.InterviewErrorCase;
import com.server.interview.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterviewSummaryQueryService {

    private final InterviewRepository interviewRepository;

    public String getSummary(Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ApplicationException(InterviewErrorCase.INTERVIEW_NOT_FOUND));

        // Interview 엔티티에 summary 필드가 있다고 가정
        String summary = interview.getSummary();

        // null 방지해서 빈 문자열로 반환
        return summary != null ? summary : "";
    }
}

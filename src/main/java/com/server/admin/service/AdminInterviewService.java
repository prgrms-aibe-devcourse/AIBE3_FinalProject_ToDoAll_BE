package com.server.admin.service;

import com.server.global.exception.ApplicationException;
import com.server.interview.domain.Interview;
import com.server.interview.domain.InterviewResult;
import com.server.interview.domain.InterviewStatus;
import com.server.interview.exception.InterviewErrorCase;
import com.server.interview.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminInterviewService {

    private final InterviewRepository interviewRepository;

    @Transactional(readOnly = true)
    public List<Interview> getAll() {
        return interviewRepository.findAll();
    }

    @Transactional
    public void softDelete(Long id) {
        Interview i = interviewRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(InterviewErrorCase.INTERVIEW_NOT_FOUND));
        i.softDelete();
    }

    @Transactional
    public void restore(Long id) {
        Interview i = interviewRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(InterviewErrorCase.INTERVIEW_NOT_FOUND));
        i.restore();
    }

    @Transactional
    public void updateStatus(Long id, InterviewStatus status) {
        Interview i = interviewRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(InterviewErrorCase.INTERVIEW_NOT_FOUND));
        i.updateStatus(status);
    }

    @Transactional
    public void updateResult(Long id, InterviewResult result) {
        Interview i = interviewRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(InterviewErrorCase.INTERVIEW_NOT_FOUND));
        i.updateResult(result);
    }
}

package com.server.admin.service;

import com.server.global.exception.ApplicationException;
import com.server.resume.domain.Resume;
import com.server.resume.exception.ResumeErrorCase;
import com.server.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminResumeService {

    private final ResumeRepository resumeRepository;

    @Transactional(readOnly = true)
    public List<Resume> getAllResumes() {
        return resumeRepository.findAllWithJobDescriptionOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Resume getResumeDetail(Long resumeId) {
        return resumeRepository.findByIdWithDetails(resumeId)
                .orElseThrow(() -> new ApplicationException(ResumeErrorCase.RESUME_NOT_FOUND));
    }

    @Transactional
    public void softDelete(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ApplicationException(ResumeErrorCase.RESUME_NOT_FOUND));
        resume.softDelete();
    }

    @Transactional
    public void restore(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ApplicationException(ResumeErrorCase.RESUME_NOT_FOUND));
        resume.restore();
    }
}

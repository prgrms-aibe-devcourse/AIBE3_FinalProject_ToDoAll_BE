package com.server.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.admin.dto.AdminResumeForm;
import com.server.global.exception.ApplicationException;
import com.server.resume.domain.Resume;
import com.server.resume.dto.ResumeCreateRequestDto;
import com.server.resume.exception.ResumeErrorCase;
import com.server.resume.repository.ResumeRepository;
import com.server.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeService resumeService;
    private final ObjectMapper objectMapper;


    @Transactional
    public Long createFromAdmin(AdminResumeForm form) {

        if (form.getJobDescriptionId() == null) {
            throw new ApplicationException(ResumeErrorCase.JD_NOT_FOUND);
        }

        try {
            // JSON → ResumeCreateRequestDto 변환
            ResumeCreateRequestDto dto = objectMapper.readValue(
                    form.toJson(),
                    ResumeCreateRequestDto.class
            );

            return resumeService.createResume(dto).id();

        } catch (Exception e) {
            throw new ApplicationException(ResumeErrorCase.INVALID_DATA);
        }
    }

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

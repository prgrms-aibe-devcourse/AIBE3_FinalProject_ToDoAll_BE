package com.server.resume.service;


import com.server.resume.domain.*;
import com.server.resume.dto.ResumeResponseDto;
import com.server.resume.exception.ResumeErrorCase;
import com.server.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;

    @Transactional(readOnly = true)
    public ResumeResponseDto getResumeById(Long resumeId) {
        Resume resume = resumeRepository.findByIdWithDetails(resumeId)
                .orElseThrow(() -> new RuntimeException(ResumeErrorCase.RESUME_NOT_FOUND.getMessage()));

        return ResumeResponseDto.fromEntity(
                resume,
                resume.getEducations().stream().map(ResumeEducation::toString).toList(),
                resume.getExperiences().stream().map(ResumeExperience::toString).toList(),
                resume.getSkills().stream().map(ResumeSkill::toString).toList(),
                resume.getActivities().stream().map(ResumeActivity::toString).toList(),
                resume.getCertifications().stream().map(ResumeCertification::toString).toList()
        );

    }
}

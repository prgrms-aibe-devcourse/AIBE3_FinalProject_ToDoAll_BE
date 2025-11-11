package com.server.resume.service;

import com.server.global.exception.ApplicationException;
import com.server.resume.domain.Resume;
import com.server.resume.domain.ResumeStatus;
import com.server.resume.dto.ResumeCreateRequestDto;
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

    // 이력서 생성 관련
    @Transactional
    public Long createResume(ResumeCreateRequestDto request) {
        Resume resume = Resume.of(
                request.name(),
                request.gender(),
                request.birthDate(),
                request.email(),
                request.phone(),
                request.address(),
                request.detailAddress(),
                request.education(),
                request.experience(),
                request.skills(),
                request.activities(),
                request.certifications(),
                request.resumeFileUrl(),
                request.portfolioFileUrl(),
                ResumeStatus.NEW
        );

        return resumeRepository.save(resume).getId();
    }

    // 이력서 조회 관련
    @Transactional(readOnly = true)
    public ResumeResponseDto getResume(Long id) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ResumeErrorCase.RESUME_NOT_FOUND));

        return new ResumeResponseDto(
                resume.getId(),
                resume.getName(),
                resume.getGender(),
                resume.getBirthDate(),
                resume.getEmail(),
                resume.getPhone(),
                resume.getAddress(),
                resume.getDetailAddress(),
                resume.getEducation(),
                resume.getExperience(),
                resume.getSkills(),
                resume.getActivities(),
                resume.getCertifications(),
                resume.getResumeFileUrl(),
                resume.getPortfolioFileUrl(),
                resume.getStatus()
        );
    }
}

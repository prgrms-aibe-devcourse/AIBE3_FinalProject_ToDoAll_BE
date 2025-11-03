package com.server.resume.service;

import com.server.global.exception.ApplicationException;
import com.server.resume.domain.Resume;
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

    // 이력서 생성 관련 서비스 로직
    @Transactional
    public Long createResume(ResumeCreateRequestDto request) {
        Resume resume = Resume.builder()
                .name(request.name())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .address(request.address())
                .education(request.education())
                .experience(request.experience())
                .skills(request.skills())
                .certifications(request.certifications())
                .resumeFileUrl(request.resumeFileUrl())
                .portfolioFileUrl(request.portfolioFileUrl())
                .userId(request.userId())
                .build();

        return resumeRepository.save(resume).getId();
    }

    // 이력서 조회 관련 서비스 로직
    @Transactional(readOnly = true)
    public ResumeResponseDto getResume(Long id) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ResumeErrorCase.RESUME_NOT_FOUND));

        return new ResumeResponseDto(
                resume.getId(),
                resume.getName(),
                resume.getEmail(),
                resume.getPhoneNumber(),
                resume.getAddress(),
                resume.getEducation(),
                resume.getExperience(),
                resume.getSkills(),
                resume.getCertifications(),
                resume.getResumeFileUrl(),
                resume.getPortfolioFileUrl()
        );
    }
}

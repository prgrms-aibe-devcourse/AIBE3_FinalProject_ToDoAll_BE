package com.server.jd.service;

import com.server.jd.domain.JobDescription;
import com.server.jd.domain.JobStatus;
import com.server.jd.dto.JobDescriptionCreateRequestDto;
import com.server.jd.repository.JobDescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JobDescriptionService {

    private final JobDescriptionRepository jobRepository;


    // JD 초안 생성 서비스 로직 (지원자 수 0 초기화 및 공고 생성 (본인이 원하는대로 수정)
    @Transactional
    public Long createDraft(JobDescriptionCreateRequestDto request) {
        JobDescription jd = JobDescription.builder()
                .title(request.title())
                .department(request.department())
                .workType(request.workType())
                .experience(request.experience())
                .education(request.education())
                .salary(request.salary())
                .description(request.description())
                .deadline(request.deadline())
                .requiredSkills(request.requiredSkills())
                .preferredSkills(request.preferredSkills())
                .welfare(request.welfare())
                .status(JobStatus.DRAFT) // 최초 생성은 무조건 초안 상태
                .applicantCount(0L) // 신규 공고이므로 지원자 수는 0
                .build();

        return jobRepository.save(jd).getId();
    }
}

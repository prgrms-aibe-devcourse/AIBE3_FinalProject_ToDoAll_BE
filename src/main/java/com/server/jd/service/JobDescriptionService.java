package com.server.jd.service;

import com.server.global.response.CommonResponse;
import com.server.jd.domain.Skill;
import com.server.global.exception.ApplicationException;
import com.server.jd.domain.JobDescription;
import com.server.jd.domain.JobPreferredSkill;
import com.server.jd.domain.JobRequiredSkill;
import com.server.jd.domain.JobStatus;
import com.server.jd.dto.*;
import com.server.jd.exception.JobErrorCase;
import com.server.jd.repository.JobDescriptionRepository;
import com.server.jd.repository.JobPreferredSkillRepository;
import com.server.jd.repository.JobRequiredSkillRepository;
import com.server.jd.repository.SkillRepository;
import com.server.jd.repository.projection.SkillByJobProjection;
import com.server.user.domain.User;
import com.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobDescriptionService {

    private final JobDescriptionRepository jobRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final JobRequiredSkillRepository jobRequiredSkillRepository;
    private final JobPreferredSkillRepository jobPreferredSkillRepository;

    // JD 초안 생성 서비스 로직 (지원자 수 0 초기화 및 공고 생성 (본인이 원하는대로 수정)
    @Transactional
    public Long createDraft(JobDescriptionCreateRequestDto request) {
        User author = userRepository.findById(request.authorId())
                .orElseThrow(() -> new ApplicationException(JobErrorCase.AUTHOR_NOT_FOUND));

        JobDescription jd = JobDescription.of(
                request.title(),
                request.department(),
                request.workType(),
                request.experience(),
                request.education(),
                request.salary(),
                request.description(),
                null, // 초안이므로 null
                request.deadline(),
                JobStatus.OPEN,
                request.benefits(),
                0L, // 지원자 수 초기값
                request.location(),
                request.thumbnailUrl(),
                author
        );

        jobRepository.save(jd);

        // 필수 기술 매핑
        List<Skill> requiredSkills = skillRepository.findByNameIn(request.requiredSkills());
        List<JobRequiredSkill> requiredSkillEntities = requiredSkills.stream()
                .map(skill -> JobRequiredSkill.of(jd, skill))
                .toList();
        jobRequiredSkillRepository.saveAll(requiredSkillEntities);

        // 우대 기술 매핑
        List<Skill> preferredSkills = skillRepository.findByNameIn(request.preferredSkills());
        List<JobPreferredSkill> preferredSkillEntities = preferredSkills.stream()
                .map(skill -> JobPreferredSkill.of(jd, skill))
                .toList();
        jobPreferredSkillRepository.saveAll(preferredSkillEntities);

        return jd.getId();
    }

    @Transactional(readOnly = true)
    public Page<JobDescriptionListResponseDto> getList(Pageable pageable, int skillLimit) {
        Page<JobDescription> page = jobRepository.findAll(pageable);
        List<Long> ids = page.stream().map(JobDescription::getId).toList();

        Map<Long, List<String>> requiredMap = jobRequiredSkillRepository
                .findRequiredSkillsByJobIds(ids).stream()
                .collect(Collectors.groupingBy(
                        SkillByJobProjection::getJobId,
                        Collectors.mapping(SkillByJobProjection::getSkillName, Collectors.toList())
                ));

        List<JobDescriptionListResponseDto> content = page.stream()
                .map(e -> JobDescriptionListResponseDto.builder()
                        .id(e.getId())
                        .title(e.getTitle())
                        .location(e.getLocation())
                        .applicantCount(Optional.ofNullable(e.getApplicantCount()).orElse(0L))
                        .status(e.getStatus())
                        .requiredSkills(requiredMap.getOrDefault(e.getId(), List.of())
                                .stream().distinct().limit(skillLimit).toList())
                        .startDate(e.getStartDate())
                        .deadline(e.getDeadline())
                        .build())
                .toList();

        return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public JobDescriptionDetailResponseDto getDetail(Long id) {
        JobDescription jd = jobRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(JobErrorCase.JOB_NOT_FOUND));

        List<String> required = jobRequiredSkillRepository
                .findRequiredSkillNamesByJobId(id).stream().distinct().toList();

        List<String> preferred = jobPreferredSkillRepository
                .findPreferredSkillNamesByJobId(id).stream().distinct().toList();

        return JobDescriptionDetailResponseDto.builder()
                .id(jd.getId())
                .title(jd.getTitle())
                .location(jd.getLocation())
                .applicantCount(jd.getApplicantCount())
                .status(jd.getStatus())
                .skills(required)
                .startDate(jd.getStartDate())
                .deadline(jd.getDeadline())
                .thumbnailUrl(jd.getThumbnailUrl())
                .description(jd.getDescription())
                .preferredSkills(preferred)
                .benefits(jd.getWelfare())
                .experience(jd.getExperience())
                .education(jd.getEducation())
                .workType(jd.getWorkType())
                .salary(jd.getSalary())
                .department(jd.getDepartment())
                .build();
    }

    @Transactional
    public JobDescriptionStatusResponseDto updateStatus(
            Long id,
            JobDescriptionStatusRequestDto request
    ) {
        JobDescription jd = jobRepository.findById(id).orElseThrow(() -> new ApplicationException(JobErrorCase.JOB_NOT_FOUND));
        jd.updateStatus(request.status());
        return new JobDescriptionStatusResponseDto(
                jd.getId(),
                jd.getStatus()
        );
    }
}

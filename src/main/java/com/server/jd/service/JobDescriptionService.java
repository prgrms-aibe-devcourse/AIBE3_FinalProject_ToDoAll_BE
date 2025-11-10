package com.server.jd.service;

import com.server.jd.domain.JobDescription;
import com.server.jd.domain.JobStatus;
import com.server.jd.dto.JobDescriptionCreateRequestDto;
import com.server.jd.dto.JobDescriptionListResponseDto;
import com.server.jd.repository.JobDescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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

    public Page<JobDescriptionListResponseDto> getList(Pageable pageable, int skillLimit) {
        Page<JobDescription> page = jobRepository.findAll(pageable);
        List<Long> ids = page.stream().map(JobDescription::getId).toList();
        Map<Long, List<String>> skillsMap = collectSkills(ids, skillLimit);
        List<JobDescriptionListResponseDto> content = page.stream()
                .map(e -> JobDescriptionListResponseDto.builder()
                        .id(e.getId())
                        .title(e.getTitle())
                        .location(null)
                        .applicantCount(Optional.ofNullable(e.getApplicantCount()).orElse(0L))
                        .status(e.getStatus())
                        .requiredSkills(skillsMap.getOrDefault(e.getId(), List.of()))
                        .startDate(e.getStartDate())
                        .deadline(e.getDeadline())
                        .build())
                .toList();
    }

    private Map<Long, List<String>> collectSkills(List<Long> ids, int limit) {
        if (ids.isEmpty()) return Map.of();

        Map<Long, List<String>> map = new HashMap<>();

        jobRepository.findRequiredSkillsByJobIds(ids).forEach(row -> {
            Long id = (Long) row[0];
            String skill = (String) row[1];
            map.computeIfAbsent(id, k -> new ArrayList<>()).add(skill);
        });

        map.replaceAll((k, v) -> v.stream()
                .filter(Objects::nonNull)
                .distinct()
                .limit(limit)
                .collect(Collectors.toList()));

        return map;
    }
}

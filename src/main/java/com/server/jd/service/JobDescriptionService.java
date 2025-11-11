package com.server.jd.service;

import com.server.global.exception.ApplicationException;
import com.server.jd.domain.JobDescription;
import com.server.jd.domain.JobStatus;
import com.server.jd.dto.JobDescriptionCreateRequestDto;
import com.server.jd.dto.JobDescriptionListResponseDto;
import com.server.jd.repository.JobDescriptionRepository;
import com.server.user.domain.User;
import com.server.user.exception.UserErrorCase;
import com.server.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.service.spi.ServiceException;
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

    // JD 초안 생성 서비스 로직 (지원자 수 0 초기화 및 공고 생성 (본인이 원하는대로 수정)
    @Transactional
    public Long createDraft(JobDescriptionCreateRequestDto request) {
        User author = userRepository.findById(request.authorId())
                .orElseThrow(() ->  new ApplicationException(UserErrorCase.USER_NOT_FOUND));

        JobDescription jd = JobDescription.of(
                request.title(),
                request.department(),
                request.workType(),
                request.experience(),
                request.education(),
                request.salary(),
                request.description(),
                null,  // startDate는 초안 단계에서 null
                request.deadline(),
                JobStatus.DRAFT,
                request.requiredSkills(),
                request.preferredSkills(),
                request.welfare(),
                0L,
                author
        );

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
        return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
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

package com.server.jd.service;

import com.server.global.auth.AuthUtils;
import com.server.global.exception.ApplicationException;
import com.server.jd.domain.*;
import com.server.jd.dto.*;
import com.server.jd.exception.JobErrorCase;
import com.server.jd.repository.JobDescriptionRepository;
import com.server.jd.repository.JobPreferredSkillRepository;
import com.server.jd.repository.JobRequiredSkillRepository;
import com.server.jd.repository.SkillRepository;
import com.server.jd.repository.projection.SkillByJobProjection;
import com.server.s3.domain.Partition;
import com.server.s3.service.PresignedUrlProvider;
import com.server.s3.service.S3Uploader;
import com.server.user.domain.User;
import com.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobDescriptionService {

    private final JobDescriptionRepository jobRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final JobRequiredSkillRepository jobRequiredSkillRepository;
    private final JobPreferredSkillRepository jobPreferredSkillRepository;
    private final S3Uploader s3Uploader;
    private final PresignedUrlProvider presignedUrlProvider;

    // JD 초안 생성 서비스 로직 (지원자 수 0 초기화 및 공고 생성 (본인이 원하는대로 수정)
    @Transactional
    public Long createDraft(JobDescriptionCreateRequestDto request) {
        User author = userRepository.findById(AuthUtils.getCurrentUserId())
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
        jobRepository.flush();

        // 필수 기술 매핑
        List<JobRequiredSkill> requiredSkillEntities = request.requiredSkills().stream()
                .map(skillName -> {
                    String normalized = skillName.trim().toLowerCase();
                    Skill skill = skillRepository.findByName(normalized)
                            .orElseGet(() -> skillRepository.save(Skill.of(normalized)));
                    return JobRequiredSkill.of(jd, skill);
                })
                .toList();
        jobRequiredSkillRepository.saveAll(requiredSkillEntities);

        // 우대 기술 매핑
        List<JobPreferredSkill> preferredSkillEntities = request.preferredSkills().stream()
                .map(skillName -> {
                    String normalized = skillName.trim().toLowerCase();
                    Skill skill = skillRepository.findByName(normalized)
                            .orElseGet(() -> skillRepository.save(Skill.of(normalized)));
                    return JobPreferredSkill.of(jd, skill);
                })
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

        String finalThumbnailUrl = null;
        String dbFileKey = jd.getThumbnailUrl();

        if (dbFileKey != null) {
            finalThumbnailUrl = presignedUrlProvider.createPresignedGetUrl(dbFileKey);
        }

        return JobDescriptionDetailResponseDto.builder()
                .id(jd.getId())
                .title(jd.getTitle())
                .location(jd.getLocation())
                .applicantCount(jd.getApplicantCount())
                .status(jd.getStatus())
                .skills(required)
                .startDate(jd.getStartDate())
                .deadline(jd.getDeadline())
                .thumbnailUrl(finalThumbnailUrl)
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

    @Transactional
    public JobDescriptionDetailResponseDto update(
            Long id,
            JobDescriptionUpdateRequestDto request
    ) {
        JobDescription jd = jobRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(JobErrorCase.JOB_NOT_FOUND));

        jd.update(request);

        jobRequiredSkillRepository.deleteByJobId(id);
        jobPreferredSkillRepository.deleteByJobId(id);

        List<String> requiredSkillNames = Optional.ofNullable(request.requiredSkills())
                .orElseGet(List::of);

        if (!requiredSkillNames.isEmpty()) {
            List<Skill> requiredSkills = skillRepository.findByNameIn(requiredSkillNames);
            List<JobRequiredSkill> requiredEntities = requiredSkills.stream()
                    .map(skill -> JobRequiredSkill.of(jd, skill))
                    .toList();
            jobRequiredSkillRepository.saveAll(requiredEntities);
        }

        List<String> preferredSkillNames = Optional.ofNullable(request.preferredSkills())
                .orElseGet(List::of);

        if (!preferredSkillNames.isEmpty()) {
            List<Skill> preferredSkills = skillRepository.findByNameIn(preferredSkillNames);
            List<JobPreferredSkill> preferredEntities = preferredSkills.stream()
                    .map(skill -> JobPreferredSkill.of(jd, skill))
                    .toList();
            jobPreferredSkillRepository.saveAll(preferredEntities);
        }

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
    public String updateThumbnail(Long id, MultipartFile thumbnailFile) {
        JobDescription jd = jobRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(JobErrorCase.JOB_NOT_FOUND));

        if (thumbnailFile == null || thumbnailFile.isEmpty()) {
            // 파일이 없으면 기존 썸네일 URL을 null로 업데이트하고 반환 (선택 사항)
            if (jd.getThumbnailUrl() != null) {
                s3Uploader.deleteFile(jd.getThumbnailUrl());
            }
            jd.updateThumbnailUrl(null);
            return null;
        }

        String currentFileKey = jd.getThumbnailUrl();
        String relativeId = String.valueOf(id);
        String newFileKey;

        if (currentFileKey != null && !currentFileKey.isBlank()) {
            // 기존 파일이 있으면, updateFile로 교체 (기존 파일 삭제 포함)
            newFileKey = s3Uploader.updateFile(thumbnailFile, currentFileKey);
        } else {
            // 기존 파일이 없으면, 새로 업로드
            newFileKey = s3Uploader.uploadFile(thumbnailFile, Partition.JOB, relativeId, "thumbnail");
        }

        // 엔티티에 최종 File Key 업데이트
        jd.updateThumbnailUrl(newFileKey);

        return newFileKey;
    }

    @Transactional(readOnly = true)
    public List<JobDescriptionOptionDto> getMyOptionJdList() {
        Long userId = AuthUtils.getCurrentUserId();

        return jobRepository.findByAuthorId(userId)
                .stream()
                .map(JobDescriptionOptionDto::from)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<JobDescriptionInterviewOptionDto> getMyInterviewOptionJdList() {
        Long userId = AuthUtils.getCurrentUserId();
        return jobRepository.findJdListByInterviewParticipant(userId);
    }

    @Transactional(readOnly = true)
    public Page<JobDescriptionListResponseDto> getMyList(Pageable pageable, int skillLimit) {
        Long userId = AuthUtils.getCurrentUserId();
        Page<JobDescription> page = jobRepository.findAllByAuthorId(userId, pageable);
        List<Long> ids = page.stream().map(JobDescription::getId).toList();

        Map<Long, List<String>> requiredMap = jobRequiredSkillRepository
                .findRequiredSkillsByJobIds(ids).stream()
                .collect(Collectors.groupingBy(
                        SkillByJobProjection::getJobId,
                        Collectors.mapping(SkillByJobProjection::getSkillName, Collectors.toList())
                ));

        List<JobDescriptionListResponseDto> content = page.stream()
                .map(e -> {
                    // 💡 썸네일 URL 변환 로직 추가
                    String finalThumbnailUrl = null;
                    String dbFileKey = e.getThumbnailUrl(); // JobDescription 엔티티에서 File Key를 가져옴

                    if (dbFileKey != null) {
                        finalThumbnailUrl = presignedUrlProvider.createPresignedGetUrl(dbFileKey);
                    }

                    return JobDescriptionListResponseDto.builder()
                            .id(e.getId())
                            .title(e.getTitle())
                            .location(e.getLocation())
                            .applicantCount(Optional.ofNullable(e.getApplicantCount()).orElse(0L))
                            .status(e.getStatus())
                            .requiredSkills(requiredMap.getOrDefault(e.getId(), List.of())
                                    .stream().distinct().limit(skillLimit).toList())
                            .startDate(e.getStartDate())
                            .deadline(e.getDeadline())
                            .thumbnailUrl(finalThumbnailUrl)
                            .build();
                })
                .toList();

        return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
    }


}

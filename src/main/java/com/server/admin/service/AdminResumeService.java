package com.server.admin.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.admin.dto.AdminResumeForm;
import com.server.global.exception.ApplicationException;
import com.server.jd.domain.JobDescription;
import com.server.jd.repository.JobDescriptionRepository;
import com.server.jd.domain.Skill;
import com.server.jd.repository.SkillRepository;
import com.server.resume.domain.Resume;
import com.server.resume.dto.*;
import com.server.resume.exception.ResumeErrorCase;
import com.server.resume.repository.ResumeRepository;
import com.server.resume.service.ResumeService;
import com.server.search.service.ResumeSearchService;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class AdminResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeService resumeService;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final SkillRepository skillRepository;
    private final ResumeSearchService resumeSearchService;
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
        Resume resume = resumeRepository.findByIdWithDetails(resumeId)
                .orElseThrow(() -> new ApplicationException(ResumeErrorCase.RESUME_NOT_FOUND));

        Hibernate.initialize(resume.getEducations());
        Hibernate.initialize(resume.getExperiences());
        Hibernate.initialize(resume.getActivities());
        Hibernate.initialize(resume.getCertifications());
        Hibernate.initialize(resume.getSkills());
        
        resume.getSkills().forEach(rs -> Hibernate.initialize(rs.getSkill()));

        return resume;
    }

    @Transactional
    public void updateFromAdmin(Long id, AdminResumeForm form) {
        Resume resume = resumeRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ApplicationException(ResumeErrorCase.RESUME_NOT_FOUND));

        JobDescription jd = jobDescriptionRepository.findById(form.getJobDescriptionId())
                .orElseThrow(() -> new ApplicationException(ResumeErrorCase.JD_NOT_FOUND));

        // 기본 필드 업데이트
        resume.updateBasicInfo(
                jd,
                form.getName(),
                form.getGender(),
                LocalDate.parse(form.getBirthDate()),
                form.getEmail(),
                form.getPhone(),
                form.getAddress(),
                form.getDetailAddress(),
                form.getResumeFileUrl(),
                form.getPortfolioFileUrl()
        );

        resume.getEducations().clear();
        resume.getExperiences().clear();
        resume.getSkills().clear();
        resume.getActivities().clear();
        resume.getCertifications().clear();

        // JSON → DTO 리스트 파싱
        parseAndApply(form.getEducationJson(),
                new TypeReference<List<ResumeEducationRequestDto>>() {},
                list -> addEducations(resume, list));

        parseAndApply(form.getExperienceJson(),
                new TypeReference<List<ResumeExperienceRequestDto>>() {},
                list -> addExperiences(resume, list));

        parseAndApply(form.getSkillsJson(),
                new TypeReference<List<ResumeSkillRequestDto>>() {},
                list -> addSkills(resume, list));

        parseAndApply(form.getActivitiesJson(),
                new TypeReference<List<ResumeActivityRequestDto>>() {},
                list -> addActivities(resume, list));

        parseAndApply(form.getCertificationsJson(),
                new TypeReference<List<ResumeCertificationRequestDto>>() {},
                list -> addCertifications(resume, list));
        // ES 문서 동기화
        resumeSearchService.index(resume);
    }

    private <T> void parseAndApply(String json,
                                   TypeReference<List<T>> typeRef,
                                   Consumer<List<T>> consumer) {
        if (!StringUtils.hasText(json)) return;
        try {
            List<T> parsed = objectMapper.readValue(json, typeRef);
            consumer.accept(parsed);
        } catch (Exception e) {
            throw new ApplicationException(ResumeErrorCase.INVALID_DATA);
        }
    }

    private void addEducations(Resume resume, List<ResumeEducationRequestDto> dtos) {
        for (ResumeEducationRequestDto dto : dtos) {
            resume.addEducation(
                    dto.educationLevel(),
                    dto.schoolName(),
                    dto.major(),
                    dto.isGraduated(),
                    dto.admissionDate(),
                    dto.graduationDate(),
                    dto.attendanceType(),
                    dto.gpa(),
                    dto.gpaScale()
            );
        }
    }

    private void addExperiences(Resume resume, List<ResumeExperienceRequestDto> dtos) {
        for (ResumeExperienceRequestDto dto : dtos) {
            resume.addExperience(
                    dto.companyName(),
                    dto.department(),
                    dto.position(),
                    dto.startDate(),
                    dto.endDate()
            );
        }
    }

    private void addSkills(Resume resume, List<ResumeSkillRequestDto> dtos) {
        for (ResumeSkillRequestDto dto : dtos) {
            String normalized = dto.skillName().trim().toLowerCase();
            Skill skill = skillRepository.findByName(normalized)
                    .orElseGet(() -> skillRepository.save(Skill.of(normalized)));
            resume.addSkill(skill, dto.proficiencyLevel());
        }
    }

    private void addActivities(Resume resume, List<ResumeActivityRequestDto> dtos) {
        for (ResumeActivityRequestDto dto : dtos) {
            resume.addActivity(
                    dto.title(),
                    dto.type(),
                    dto.organization()
            );
        }
    }

    private void addCertifications(Resume resume, List<ResumeCertificationRequestDto> dtos) {
        for (ResumeCertificationRequestDto dto : dtos) {
            resume.addCertification(
                    dto.type(),
                    dto.name(),
                    dto.scoreOrLevel()
            );
        }
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

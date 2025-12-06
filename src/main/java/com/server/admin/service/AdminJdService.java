package com.server.admin.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.admin.dto.AdminJdForm;
import com.server.global.exception.ApplicationException;
import com.server.jd.domain.*;
import com.server.jd.exception.JobErrorCase;
import com.server.jd.repository.JobDescriptionRepository;
import com.server.jd.repository.JobPreferredSkillRepository;
import com.server.jd.repository.JobRequiredSkillRepository;
import com.server.jd.repository.SkillRepository;
import com.server.resume.repository.ResumeRepository;
import com.server.user.domain.User;
import com.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminJdService {

    private final JobDescriptionRepository jdRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final JobRequiredSkillRepository jobRequiredSkillRepository;
    private final JobPreferredSkillRepository jobPreferredSkillRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<JobDescription> getAll() {
        List<JobDescription> jds = jdRepository.findAll();

        jds.forEach(jd -> {
            Long count = resumeRepository.countByJobDescriptionId(jd.getId());
            jd.applyApplicantCount(count);
        });

        return jds;
    }

    @Transactional(readOnly = true)
    public JobDescription getDetail(Long id) {
        return jdRepository.findByIdFetchSkills(id)
                .orElseThrow(() -> new ApplicationException(JobErrorCase.JOB_NOT_FOUND));
    }

    @Transactional
    public Long createFromAdmin(AdminJdForm form) {
        User author = userRepository.findById(form.getAuthorId())
                .orElseThrow(() -> new ApplicationException(JobErrorCase.AUTHOR_NOT_FOUND));

        LocalDate deadline = null;
        if (StringUtils.hasText(form.getDeadline())) {
            deadline = LocalDate.parse(form.getDeadline());
        }

        JobDescription jd = JobDescription.of(
                form.getTitle(),
                form.getDepartment(),
                form.getWorkType(),
                form.getExperience(),
                form.getEducation(),
                form.getSalary(),
                form.getDescription(),
                null,
                deadline,
                JobStatus.OPEN,
                form.getBenefits(),
                0L,
                form.getLocation(),
                form.getThumbnailUrl(),
                author
        );

        jdRepository.save(jd);
        jdRepository.flush();

        // JSON 배열 또는 CSV 모두 처리
        List<String> required = parseSkillsFlexible(form.getRequiredSkills());
        List<String> preferred = parseSkillsFlexible(form.getPreferredSkills());

        saveRequiredSkills(jd, required);
        savePreferredSkills(jd, preferred);

        return jd.getId();
    }

    private void saveRequiredSkills(JobDescription jd, List<String> skills) {
        for (String skillName : skills) {
            String normalized = skillName.trim().toLowerCase();
            Skill skill = skillRepository.findByName(normalized)
                    .orElseGet(() -> skillRepository.save(Skill.of(normalized)));
            jobRequiredSkillRepository.save(JobRequiredSkill.of(jd, skill));
        }
    }

    private void savePreferredSkills(JobDescription jd, List<String> skills) {
        for (String skillName : skills) {
            String normalized = skillName.trim().toLowerCase();
            Skill skill = skillRepository.findByName(normalized)
                    .orElseGet(() -> skillRepository.save(Skill.of(normalized)));
            jobPreferredSkillRepository.save(JobPreferredSkill.of(jd, skill));
        }
    }

    private List<String> parseSkillsFlexible(String input) {
        if (!StringUtils.hasText(input)) return List.of();

        try {
            // JSON
            return objectMapper.readValue(input, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            // CSV
            return Arrays.stream(input.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
    }

    @Transactional
    public void softDelete(Long id) {
        JobDescription jd = jdRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(JobErrorCase.JOB_NOT_FOUND));
        jd.softDelete();
    }

    @Transactional
    public void restore(Long id) {
        JobDescription jd = jdRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(JobErrorCase.JOB_NOT_FOUND));
        jd.restore();
    }

    @Transactional
    public void updateStatus(Long id, JobStatus status) {
        JobDescription jd = jdRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(JobErrorCase.JOB_NOT_FOUND));
        jd.updateStatus(status);
    }
}


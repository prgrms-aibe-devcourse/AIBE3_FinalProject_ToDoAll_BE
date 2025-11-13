package com.server.resume.service;

import com.server.global.exception.ApplicationException;
import com.server.jd.domain.JobDescription;
import com.server.jd.domain.Skill;
import com.server.jd.repository.JobDescriptionRepository;
import com.server.jd.repository.SkillRepository;
import com.server.resume.domain.*;
import com.server.resume.dto.*;
import com.server.resume.exception.ResumeErrorCase;
import com.server.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final SkillRepository skillRepository;
    private final JobDescriptionRepository jobDescriptionRepository;

    @Transactional(readOnly = true)
    public ResumeResponseDto getResumeById(Long resumeId) {
        Resume resume = resumeRepository.findByIdWithDetails(resumeId)
                .orElseThrow(() -> new ApplicationException(ResumeErrorCase.RESUME_NOT_FOUND));

        return ResumeResponseDto.fromEntity(resume);
    }

    @Transactional
    public ResumeResponseDto createResume(ResumeCreateRequestDto request) {

        JobDescription jdEntity = null;
        if (request.jobDescription() == null || request.jobDescription().getId() == null) {
            throw new ApplicationException(ResumeErrorCase.JD_NOT_FOUND);
        }
        jdEntity = jobDescriptionRepository.findById(request.jobDescription().getId())
                .orElseThrow(() -> new ApplicationException(ResumeErrorCase.JD_NOT_FOUND));

        Resume resume = Resume.of(
                jdEntity,
                request.name(),
                request.gender(),
                request.birthDate(),
                request.email(),
                request.phone(),
                request.address(),
                request.detailAddress(),
                request.resumeFileUrl(),
                request.portfolioFileUrl(),
                ResumeStatus.NEW
        );

        addEducations(resume, request.education());
        addExperiences(resume, request.experience());
        addSkills(resume, request.skills());
        addActivities(resume, request.activities());
        addCertifications(resume, request.certifications());

        Resume saved = resumeRepository.save(resume);
        return ResumeResponseDto.fromEntity(saved);
    }

    @Transactional
    public void deleteResume(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ApplicationException(ResumeErrorCase.RESUME_NOT_FOUND));
        resumeRepository.delete(resume);
    }

    @Transactional
    public ResumeStatusUpdateResponseDto updateResumeStatus(Long resumeId, ResumeStatusUpdateDto request) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ApplicationException(ResumeErrorCase.RESUME_NOT_FOUND));

        resume.updateStatus(request.resumeStatus());

        resumeRepository.save(resume);

        return ResumeStatusUpdateResponseDto.from(resume.getId(), resume.getStatus());
    }



    private void addEducations(Resume resume, List<ResumeEducationRequestDto> educationList) {
        if (educationList == null) return;
        for (ResumeEducationRequestDto edu : educationList) {
            resume.addEducation(
                    edu.educationLevel(),
                    edu.schoolName(),
                    edu.major(),
                    edu.isGraduated(),
                    edu.admissionDate(),
                    edu.graduationDate(),
                    edu.attendanceType(),
                    edu.gpa(),
                    edu.gpaScale()
            );
        }
    }

    private void addExperiences(Resume resume, List<ResumeExperienceRequestDto> experienceList) {
        if (experienceList == null) return;
        for (ResumeExperienceRequestDto exp : experienceList) {
            resume.addExperience(
                    exp.companyName(),
                    exp.department(),
                    exp.position(),
                    exp.startDate(),
                    exp.endDate()
            );
        }
    }

    private void addSkills(Resume resume, List<ResumeSkillRequestDto> skillsList) {
        if (skillsList == null) return;
        for (ResumeSkillRequestDto sk : skillsList) {
            Skill skill = skillRepository.findByName(sk.skillName())
                    .orElseGet(() -> skillRepository.save(Skill.of(sk.skillName())));
            resume.addSkill(skill, sk.proficiencyLevel());
        }
    }

    private void addActivities(Resume resume, List<ResumeActivityRequestDto> activityList) {
        if (activityList == null) return;
        for (ResumeActivityRequestDto act : activityList) {
            resume.addActivity(
                    act.title(),
                    act.type(),
                    act.organization()
            );
        }
    }

    private void addCertifications(Resume resume, List<ResumeCertificationRequestDto> certList) {
        if (certList == null) return;
        for (ResumeCertificationRequestDto cert : certList) {
            resume.addCertification(cert.type(), cert.name(), cert.scoreOrLevel());
        }
    }
}

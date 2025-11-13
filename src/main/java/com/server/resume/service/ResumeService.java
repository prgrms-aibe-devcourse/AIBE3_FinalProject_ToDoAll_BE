package com.server.resume.service;


import com.server.global.exception.ApplicationException;
import com.server.jd.domain.JobDescription;
import com.server.jd.repository.JobDescriptionRepository;
import com.server.jd.repository.SkillRepository;
import com.server.resume.domain.*;
import com.server.resume.dto.*;
import com.server.resume.exception.ResumeErrorCase;
import com.server.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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

        return ResumeResponseDto.fromEntity(
                resume
        );

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


        if (request.education() != null) {
            for (ResumeEducationRequestDto edu : request.education()) {
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


        if (request.experience() != null) {
            for (ResumeExperienceRequestDto exp : request.experience()) {
                resume.addExperience(
                        exp.companyName(),
                        exp.department(),
                        exp.position(),
                        exp.startDate(),
                        exp.endDate()
                );
            }
        }


        if (request.skills() != null) {
            for (ResumeSkillRequestDto sk : request.skills()) {
                var skill = skillRepository.findByName(sk.skillName())
                        .orElseGet(() -> skillRepository.save(com.server.jd.domain.Skill.of(sk.skillName())));
                resume.addSkill(skill, sk.proficiencyLevel());
            }
        }



        if (request.activities() != null) {
            for (ResumeActivityRequestDto act : request.activities()) {
                resume.addActivity(
                        act.title(),
                        act.type(),
                        act.organization()
                );
            }
        }


        if (request.certifications() != null) {
            for (ResumeCertificationRequestDto cert : request.certifications()) {
                resume.addCertification(cert.type(), cert.name(), cert.scoreOrLevel());
            }
        }

        Resume saved = resumeRepository.save(resume);
        return ResumeResponseDto.fromEntity(saved);

    }

    @Transactional
    public ResumeResponseDto deleteResume(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ApplicationException(ResumeErrorCase.RESUME_NOT_FOUND));

        resumeRepository.delete(resume);

        return ResumeResponseDto.fromEntity(resume);
    }
}

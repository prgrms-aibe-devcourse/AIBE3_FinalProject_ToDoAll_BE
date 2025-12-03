package com.server.resume.service;

import com.server.global.exception.ApplicationException;
import com.server.jd.domain.JobDescription;
import com.server.jd.domain.Skill;
import com.server.jd.repository.JobDescriptionRepository;
import com.server.jd.repository.SkillRepository;
import com.server.match.domain.Match;
import com.server.match.repository.MatchRepository;
import com.server.resume.domain.*;
import com.server.resume.domain.Resume;
import com.server.resume.domain.ResumeStatus;
import com.server.resume.dto.*;
import com.server.resume.exception.ResumeErrorCase;
import com.server.resume.repository.ResumeRepository;
import com.server.search.repository.ResumeSearchRepository;
import com.server.search.service.ResumeSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final SkillRepository skillRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final ResumeSearchService resumeSearchService;
    private final ResumeSearchRepository resumeSearchRepository;
    private final MatchRepository matchRepository;

    @Transactional(readOnly = true)
    public ResumeResponseDto getResumeById(Long resumeId) {
        Resume resume = resumeRepository.findByIdWithDetails(resumeId)
                .orElseThrow(() -> new ApplicationException(ResumeErrorCase.RESUME_NOT_FOUND));

        return ResumeResponseDto.fromEntity(resume);
    }

    @Transactional
    public ResumeResponseDto createResume(ResumeCreateRequestDto request) {
        if (request.jobDescriptionId() == null) {
            throw new ApplicationException(ResumeErrorCase.JD_NOT_FOUND);
        }

        JobDescription jdEntity = jobDescriptionRepository.findById(request.jobDescriptionId())
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
        Resume savedResume = resumeRepository.save(resume);

        if (!matchRepository.existsByJobDescription_IdAndResume_Id(jdEntity.getId(), savedResume.getId())) {
            Match match = Match.ofForApplication(jdEntity, savedResume); // APPLIED 형태로 matches 테이블에 저장
            matchRepository.save(match);
        }

        addEducations(savedResume, request.education());
        addExperiences(savedResume, request.experience());
        addSkills(savedResume, request.skills());
        addActivities(savedResume, request.activities());
        addCertifications(savedResume, request.certifications());

        resumeSearchService.index(savedResume);

        return ResumeResponseDto.fromEntity(savedResume);
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
            Skill skill = getOrCreateSkill(sk.skillName());
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


    public Skill getOrCreateSkill(String skillName) {
        return skillRepository.findByName(skillName)
                .orElseGet(() -> {
                    try {
                        return skillRepository.save(Skill.of(skillName));
                    } catch (DataIntegrityViolationException e) {
                        return skillRepository.findByName(skillName).orElseThrow();
                    }
                });
    }

    @Transactional
    public ResumeMemoResponseDto updateResumeMemo(Long resumeId, ResumeMemoRequestDto request) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ApplicationException(ResumeErrorCase.RESUME_NOT_FOUND));

        resume.updateMemo(request.memo());

        return ResumeMemoResponseDto.from(resume);

    }

    @Transactional(readOnly = true)
    public ResumeInterviewInfoResponseDto getResumeInterviewInfo(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ApplicationException(ResumeErrorCase.RESUME_NOT_FOUND));

        return new ResumeInterviewInfoResponseDto(
                resume.getName(),
                resume.getEmail(),
                resume.getPhone(),
                resume.getBirthDate(),
                resume.getPortfolioFileUrl(),
                resume.getJobDescription().getTitle()
        );
    }
}

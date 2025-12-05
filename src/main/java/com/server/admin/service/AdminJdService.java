package com.server.admin.service;

import com.server.global.exception.ApplicationException;
import com.server.jd.domain.JobDescription;
import com.server.jd.domain.JobStatus;
import com.server.jd.exception.JobErrorCase;
import com.server.jd.repository.JobDescriptionRepository;
import com.server.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminJdService {

    private final JobDescriptionRepository jdRepository;
    private final ResumeRepository resumeRepository;

    @Transactional(readOnly = true)
    public List<JobDescription> getAll() {
        List<JobDescription> jds = jdRepository.findAll();

        jds.forEach(jd -> {
            Long count = resumeRepository.countByJobDescriptionId(jd.getId());
            jd.applyApplicantCount(count);
        });

        return jds;
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


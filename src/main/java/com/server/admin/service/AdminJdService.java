package com.server.admin.service;

import com.server.jd.domain.JobDescription;
import com.server.jd.domain.JobStatus;
import com.server.jd.repository.JobDescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminJdService {

    private final JobDescriptionRepository jdRepository;

    public List<JobDescription> getAll() {
        return jdRepository.findAll();
    }

    @Transactional
    public void softDelete(Long id) {
        JobDescription jd = jdRepository.findById(id).orElseThrow();
        jd.softDelete();
    }

    @Transactional
    public void restore(Long id) {
        JobDescription jd = jdRepository.findById(id).orElseThrow();
        jd.restore();
    }

    @Transactional
    public void updateStatus(Long id, JobStatus status) {
        JobDescription jd = jdRepository.findById(id).orElseThrow();
        jd.updateStatus(status);
    }
}


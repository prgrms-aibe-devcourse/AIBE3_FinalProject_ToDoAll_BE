package com.server.admin.service;

import com.server.interview.repository.InterviewRepository;
import com.server.jd.repository.JobDescriptionRepository;
import com.server.resume.repository.ResumeRepository;
import com.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final JobDescriptionRepository jdRepository;
    private final ResumeRepository resumeRepository;
    private final InterviewRepository interviewRepository;

    public long countJds() {
        return jdRepository.count();
    }

    public long countResumes() {
        return resumeRepository.count();
    }

    public long countInterviews() {
        return interviewRepository.count();
    }
}
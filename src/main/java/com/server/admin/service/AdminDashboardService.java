package com.server.admin.service;

import com.server.admin.dto.AdminDashboardSummaryDto;
import com.server.admin.dto.RecentJdDto;
import com.server.admin.dto.RecentUserDto;
import com.server.interview.repository.InterviewRepository;
import com.server.jd.repository.JobDescriptionRepository;
import com.server.resume.repository.ResumeRepository;
import com.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final JobDescriptionRepository jdRepository;
    private final ResumeRepository resumeRepository;
    private final InterviewRepository interviewRepository;

    public AdminDashboardSummaryDto getSummary() {
        long totalUsers = userRepository.countByDeletedAtIsNull();
        long todaySignups = userRepository.countJoinedToday(LocalDate.now().atStartOfDay());

        long totalJds = jdRepository.count();
        long openJds = jdRepository.countByStatus("OPEN");
        long closedJds = jdRepository.countByStatus("CLOSED");

        long totalResumes = resumeRepository.count();
        long totalInterviews = interviewRepository.count();
        long todayInterviews = interviewRepository.countToday(LocalDate.now().atStartOfDay());

        return new AdminDashboardSummaryDto(
                totalUsers, todaySignups,
                totalJds, openJds, closedJds,
                totalResumes,
                totalInterviews, todayInterviews
        );
    }

    public List<RecentUserDto> getRecentUsers() {
        return userRepository.findTop5ByDeletedAtIsNullOrderByCreatedAtDesc()
                .stream()
                .map(RecentUserDto::from)
                .toList();
    }

    public List<RecentJdDto> getRecentJds() {
        return jdRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(RecentJdDto::from)
                .toList();
    }
}
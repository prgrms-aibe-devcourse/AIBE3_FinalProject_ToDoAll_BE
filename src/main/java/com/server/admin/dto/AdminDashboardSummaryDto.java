package com.server.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminDashboardSummaryDto {

    private long totalUsers;
    private long todaySignups;

    private long totalJds;
    private long openJds;
    private long closedJds;

    private long totalResumes;

    private long totalInterviews;
    private long todayInterviews;
}


package com.server.dashboard.dto;

import java.time.LocalDateTime;

import static com.server.dashboard.util.Formatter.formatterDateDividedSlash;
import static com.server.dashboard.util.Formatter.formatterTime;

public record DashboardUpcomingInterviewsResponseDto(
        String interviewDate,
        String applicantName,
        String jobTitle,
        String interviewTime,
        String interviewers
) {
    public static DashboardUpcomingInterviewsResponseDto from(
            LocalDateTime interviewDateTime,
            String applicantName,
            String jobTitle,
            String interviewers
    ) {
        return new DashboardUpcomingInterviewsResponseDto(
                interviewDateTime.format(formatterDateDividedSlash),
                applicantName,
                jobTitle,
                interviewDateTime.format(formatterTime),
                interviewers
        );
    }
}

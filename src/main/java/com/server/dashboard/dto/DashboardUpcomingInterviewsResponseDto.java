package com.server.dashboard.dto;

import java.time.LocalDateTime;

import static com.server.dashboard.util.Formatter.formatterDateDividedSlash;
import static com.server.dashboard.util.Formatter.formatterTime;

public record DashboardUpcomingInterviewsResponseDto(
        boolean isOrganizer,
        String interviewDate,
        String applicantName,
        String jobTitle,
        String interviewTime,
        String interviewers
) {
    public static DashboardUpcomingInterviewsResponseDto from(
            boolean isOrganizer,
            LocalDateTime interviewDateTime,
            String applicantName,
            String jobTitle,
            String interviewers
    ) {
        return new DashboardUpcomingInterviewsResponseDto(
                isOrganizer,
                interviewDateTime.format(formatterDateDividedSlash),
                applicantName,
                jobTitle,
                interviewDateTime.format(formatterTime),
                interviewers
        );
    }
}

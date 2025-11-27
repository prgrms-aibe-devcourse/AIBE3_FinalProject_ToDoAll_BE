package com.server.dashboard.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static com.server.dashboard.util.Formatter.formatterDateDividedSlash;
import static com.server.dashboard.util.Formatter.formatterTime;


public record DashboardUpcomingInterviewResponseDto(
        String interviewDate,
        String applicantName,
        String jobTitle,
        String interviewTime,
        String interviewers
) {
    public static DashboardUpcomingInterviewResponseDto from(
            LocalDateTime interviewDateTime,
            String applicantName,
            String jobTitle,
            ArrayList<String> interviewers
    ) {


        return new DashboardUpcomingInterviewResponseDto(
                interviewDateTime.format(formatterDateDividedSlash),
                applicantName,
                jobTitle,
                interviewDateTime.format(formatterTime),
                String.join(", ", interviewers)
        );
    }
}

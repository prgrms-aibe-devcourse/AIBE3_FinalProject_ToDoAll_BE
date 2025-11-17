package com.server.dashboard.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public record UpcomingInterviewDto(
        String interviewDate,
        String applicantName,
        String jobTitle,
        String interviewTime,
        String interviewers
) {
    public static UpcomingInterviewDto from(
            LocalDateTime interviewDateTime,
            String applicantName,
            String jobTitle,
            ArrayList<String> interviewers
    ) {
        DateTimeFormatter formatterDay = DateTimeFormatter.ofPattern("MM/dd");
        DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm");

        return new UpcomingInterviewDto(
                interviewDateTime.format(formatterDay),
                applicantName,
                jobTitle,
                interviewDateTime.format(formatterTime),
                String.join(", ", interviewers)
        );
    }
}

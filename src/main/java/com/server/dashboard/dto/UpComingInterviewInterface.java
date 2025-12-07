package com.server.dashboard.dto;

import java.time.LocalDateTime;

public interface UpComingInterviewInterface {
    long getInterviewId();
    LocalDateTime getScheduledTime();
    String getJobTitle();
    String getApplicantName();
    String getInterviewerName();
}

package com.server.dashboard.dto;

import com.server.dashboard.type.JobStatusOfProgress;

public interface JobStatsInterface {
    String getTitle();
    JobStatusOfProgress getStatus();
    int getApplicantCount();
    int getBookmarkCount();
    int getInterviewCount();
    int getPassCount();
}

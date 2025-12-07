package com.server.dashboard.dto;


import com.server.dashboard.type.JobStatusOfProgress;

import java.util.ArrayList;

public record DashboardApplicantStatsResponseDto(
        String title,
        ArrayList<Integer> slotData,
        JobStatusOfProgress status
) {

}

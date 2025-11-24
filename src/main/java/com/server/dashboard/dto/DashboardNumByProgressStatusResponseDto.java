package com.server.dashboard.dto;

public record DashboardNumByProgressStatusResponseDto(
        int in,
        int before,
        int close
) {
}

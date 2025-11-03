package com.server.dashboard.dto;

public record DashboardSummaryDto(
        // 예시입니다. 본인이 대시보드 관련 받아야하는 필드를 구성해주세요.
        int totalJdCount,
        int totalResumeCount,
        int totalMatches,
        double matchRate
) {}

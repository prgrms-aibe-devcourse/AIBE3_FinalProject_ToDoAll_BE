package com.server.dashboard.service;

import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    public String getSummary() {
        // TODO: JD 수, 이력서 수, 추천 수, 매칭률 등 집계 조회 로직
        return "Dashboard summary";
    }
}
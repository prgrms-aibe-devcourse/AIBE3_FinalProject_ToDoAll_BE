package com.server.admin.controller;

import com.server.admin.dto.AdminDashboardSummaryDto;
import com.server.admin.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping
    public String dashboard(Model model) {
        log.info("[ADMIN] Dashboard requested");

        AdminDashboardSummaryDto summary = dashboardService.getSummary();
        model.addAttribute("summary", summary);
        model.addAttribute("recentUsers", dashboardService.getRecentUsers());
        model.addAttribute("recentJds", dashboardService.getRecentJds());

        return "admin/dashboard";
    }
}

package com.server.admin.controller;

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

        model.addAttribute("userCount", dashboardService.countActiveUsers());
        model.addAttribute("jdCount", dashboardService.countJds());
        model.addAttribute("resumeCount", dashboardService.countResumes());
        model.addAttribute("interviewCount", dashboardService.countInterviews());

        return "admin/dashboard";
    }
}

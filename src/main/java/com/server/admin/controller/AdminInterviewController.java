package com.server.admin.controller;

import com.server.admin.dto.AdminInterviewDetailDto;
import com.server.admin.dto.AdminInterviewListDto;
import com.server.admin.service.AdminInterviewService;
import com.server.interview.service.InterviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/interviews")
public class AdminInterviewController {

    private final AdminInterviewService adminInterviewService;
    private final InterviewService interviewService;

    @GetMapping
    public String list(Model model) {
        var list = adminInterviewService.getAll()
                .stream().map(AdminInterviewListDto::from).toList();
        model.addAttribute("interviews", list);
        return "admin/interview/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var interview = adminInterviewService.getAll().stream()
                .filter(i -> i.getId().equals(id)).findFirst().orElseThrow();
        var profile = interviewService.getInterviewProfile(id);

        model.addAttribute("interview", AdminInterviewDetailDto.of(
                interview,
                profile.skills(),
                profile.missingSkills(),
                profile.experiences()
        ));
        model.addAttribute("allStatuses", com.server.interview.domain.InterviewStatus.values());
        model.addAttribute("allResults", com.server.interview.domain.InterviewResult.values());
        return "admin/interview/detail";
    }

    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable Long id, @RequestParam String status) {
        adminInterviewService.updateStatus(id, com.server.interview.domain.InterviewStatus.valueOf(status));
        return "redirect:/admin/interviews/" + id;
    }

    @PostMapping("/{id}/result")
    public String changeResult(@PathVariable Long id, @RequestParam String result) {
        adminInterviewService.updateResult(id, com.server.interview.domain.InterviewResult.valueOf(result));
        return "redirect:/admin/interviews/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        adminInterviewService.softDelete(id);
        return "redirect:/admin/interviews/" + id;
    }

    @PostMapping("/{id}/restore")
    public String restore(@PathVariable Long id) {
        adminInterviewService.restore(id);
        return "redirect:/admin/interviews/" + id;
    }
}

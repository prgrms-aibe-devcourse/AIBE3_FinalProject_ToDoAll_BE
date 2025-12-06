package com.server.admin.controller;

import com.server.admin.dto.AdminResumeForm;
import com.server.admin.service.AdminResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/resumes")
public class AdminResumeController {

    private final AdminResumeService adminResumeService;

    // 이력서 목록
    @GetMapping
    public String list(Model model) {
        model.addAttribute("resumes", adminResumeService.getAllResumes());
        return "admin/resumes/list";
    }

    // 이력서 상세
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("resume", adminResumeService.getResumeDetail(id));
        return "admin/resumes/detail";
    }

    // 이력서 신규 생성
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("resumeForm", new AdminResumeForm());
        return "admin/resumes/form";
    }

    // 이력서 생성 요청
    @PostMapping("/new")
    public String create(@ModelAttribute("resumeForm") AdminResumeForm form) {
        Long id = adminResumeService.createFromAdmin(form);
        return "redirect:/admin/resumes/" + id;
    }

    // 소프트 삭제
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        adminResumeService.softDelete(id);
        return "redirect:/admin/resumes";
    }

    // 삭제 복구
    @PostMapping("/{id}/restore")
    public String restore(@PathVariable Long id) {
        adminResumeService.restore(id);
        return "redirect:/admin/resumes";
    }
}

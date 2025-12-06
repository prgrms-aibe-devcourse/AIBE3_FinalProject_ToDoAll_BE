package com.server.admin.controller;

import com.server.admin.dto.AdminJdForm;
import com.server.admin.service.AdminJdService;
import com.server.jd.domain.JobDescription;
import com.server.jd.domain.JobStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/jds")
public class AdminJdController {

    private final AdminJdService adminJdService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("jds", adminJdService.getAll());
        model.addAttribute("statuses", JobStatus.values());
        return "admin/jds/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        JobDescription jd = adminJdService.getDetail(id);
        model.addAttribute("jd", jd);
        return "admin/jds/detail";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        AdminJdForm form = new AdminJdForm();
        model.addAttribute("jdForm", form);
        return "admin/jds/form";
    }

    @PostMapping("/new")
    public String create(@ModelAttribute("jdForm") AdminJdForm form) {
        Long id = adminJdService.createFromAdmin(form);
        return "redirect:/admin/jds/" + id;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        JobDescription jd = adminJdService.getDetail(id);
        AdminJdForm form = AdminJdForm.from(jd);
        model.addAttribute("jdForm", form);
        model.addAttribute("jdId", id);
        return "admin/jds/edit-form";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        adminJdService.softDelete(id);
        return "redirect:/admin/jds";
    }

    @PostMapping("/{id}/restore")
    public String restore(@PathVariable Long id) {
        adminJdService.restore(id);
        return "redirect:/admin/jds";
    }

    @PostMapping("/{id}/status")
    public String changeStatus(
            @PathVariable Long id,
            @RequestParam("status") JobStatus status
    ) {
        adminJdService.updateStatus(id, status);
        return "redirect:/admin/jds";
    }
}

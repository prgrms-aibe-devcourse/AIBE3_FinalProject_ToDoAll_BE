package com.server.admin.controller;

import com.server.admin.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    // 유저 목록 조회
    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", adminUserService.getAllUsers());
        return "admin/users/list";
    }

    // 소프트 삭제
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        adminUserService.softDelete(id);
        return "redirect:/admin/users";
    }

    // 소프트 삭제 복구
    @PostMapping("/{id}/restore")
    public String restore(@PathVariable Long id) {
        adminUserService.restore(id);
        return "redirect:/admin/users";
    }
}

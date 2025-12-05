package com.server.admin.controller;

import com.server.admin.dto.AdminMatchDetailDto;
import com.server.admin.dto.AdminMatchListDto;
import com.server.admin.service.AdminMatchService;
import com.server.match.domain.MatchStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/matches")
public class AdminMatchController {

    private final AdminMatchService adminMatchService;

    // 매칭 리스트
    @GetMapping
    public String list(Model model) {
        List<AdminMatchListDto> matches = adminMatchService.getAllMatches();
        model.addAttribute("matches", matches);
        model.addAttribute("allStatuses", MatchStatus.values());
        return "admin/matches/list";
    }

    // 매칭 상세
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        AdminMatchDetailDto match = adminMatchService.getDetail(id);
        model.addAttribute("match", match);
        model.addAttribute("allStatuses", MatchStatus.values());
        return "admin/matches/detail";
    }

    // 상태 변경
    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam("status") MatchStatus status
    ) {
        adminMatchService.updateStatus(id, status);
        return "redirect:/admin/matches/" + id;
    }

    // 소프트 삭제
    @PostMapping("/{id}/delete")
    public String softDelete(@PathVariable Long id) {
        adminMatchService.softDelete(id);
        return "redirect:/admin/matches";
    }

    // 복구
    @PostMapping("/{id}/restore")
    public String restore(@PathVariable Long id) {
        adminMatchService.restore(id);
        return "redirect:/admin/matches";
    }
}

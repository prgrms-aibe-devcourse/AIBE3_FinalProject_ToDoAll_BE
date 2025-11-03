package com.server.resume.controller;

import com.server.global.response.ApiResponse;
import com.server.resume.dto.ResumeCreateRequestDto;
import com.server.resume.dto.ResumeResponseDto;
import com.server.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    // 이력서 생성 관련 컨트롤러 로직 (로직들 더 추가해주세요.)
    @PostMapping
    public ApiResponse<Long> createResume(@RequestBody ResumeCreateRequestDto request) {
        return ApiResponse.success(resumeService.createResume(request));
    }

    // 이력서 조회 관련 컨트롤러 로직 (로직들 더 추가해주세요.)
    @GetMapping("/{id}")
    public ApiResponse<ResumeResponseDto> getResume(@PathVariable Long id) {
        return ApiResponse.success(resumeService.getResume(id));
    }
}
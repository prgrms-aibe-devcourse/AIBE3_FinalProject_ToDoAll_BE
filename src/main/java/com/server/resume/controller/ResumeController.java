package com.server.resume.controller;

import com.server.global.response.CommonResponse;
import com.server.jd.dto.JobDescriptionDetailResponseDto;
import com.server.resume.domain.Resume;
import com.server.resume.dto.ResumeResponseDto;
import com.server.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    @GetMapping("/{resumeId}")
    public ResponseEntity<CommonResponse<ResumeResponseDto>> getResume(@PathVariable Long resumeId) {
        return ResponseEntity.ok(CommonResponse.success(resumeService.getResumeById(resumeId)));
    }

}
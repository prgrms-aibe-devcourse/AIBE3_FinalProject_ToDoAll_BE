package com.server.resume.controller;

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
    public ResponseEntity<ResumeResponseDto> getResume(@PathVariable Long resumeId) {
        return ResponseEntity.ok(resumeService.getResumeById(resumeId));
    }
}
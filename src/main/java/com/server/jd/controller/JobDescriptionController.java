package com.server.jd.controller;


import com.amazonaws.Response;
import com.server.global.response.CommonResponse;
import com.server.jd.dto.*;
import com.server.jd.service.JobDescriptionService;
import com.server.jd.service.SkillQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/jd")
public class JobDescriptionController {
    private final JobDescriptionService jobService;
    private final SkillQueryService skillQueryService;

    @GetMapping
    public ResponseEntity<CommonResponse<Page<JobDescriptionListResponseDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10")int size
    ) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be greater than 0");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return ResponseEntity.ok(CommonResponse.success(jobService.getList(pageable, 5)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<JobDescriptionDetailResponseDto>> get(@PathVariable Long id) {
        return ResponseEntity.ok(CommonResponse.success(jobService.getDetail(id)));
    }

    @PostMapping
    public ResponseEntity<CommonResponse<Long>> createDraft(@Valid @RequestBody JobDescriptionCreateRequestDto request) {
        Long id = jobService.createDraft(request);
        return ResponseEntity
                .created(URI.create("/api/v1/jd/" + id))
                .body(CommonResponse.success(id));
    }

    @GetMapping("/skills")
    public ResponseEntity<CommonResponse<List<SkillResponseDto>>> getSkills() {
        List<SkillResponseDto> Skills = skillQueryService.getSkills();
        return ResponseEntity.ok(CommonResponse.success(Skills));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CommonResponse<JobDescriptionStatusResponseDto>> updateStatus(
            @PathVariable Long id,
            @RequestBody JobDescriptionStatusRequestDto request
    ) {
        JobDescriptionStatusResponseDto dto = jobService.updateStatus(id, request);
        return ResponseEntity.ok(CommonResponse.success(dto));
    }
}

package com.server.jd.controller;


import com.server.global.response.ApiResponse;
import com.server.jd.dto.JobDescriptionDetailResponseDto;
import com.server.jd.dto.JobDescriptionListResponseDto;
import com.server.jd.service.JobDescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/jd")
public class JobDescriptionController {
    private final JobDescriptionService jobService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<JobDescriptionListResponseDto>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10")int size
    ) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be greater than 0");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return ResponseEntity.ok(ApiResponse.success(jobService.getList(pageable, 5)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobDescriptionDetailResponseDto>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(jobService.getDetail(id)));
    }
}

package com.server.jd.controller;


import com.server.jd.dto.JobDescriptionListResponseDto;
import com.server.jd.service.JobDescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/jd")
public class JobDescriptionController {
    private final JobDescriptionService jobService;

    @GetMapping
    public Page<JobDescriptionListResponseDto> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10")int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return jobService.getList(pageable, 5);
    }
}

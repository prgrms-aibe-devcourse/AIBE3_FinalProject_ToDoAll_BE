package com.server.jd.controller;


import com.server.jd.service.JobDescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/jd")
public class JobDescriptionController {
    private final JobDescriptionService jobService;
}

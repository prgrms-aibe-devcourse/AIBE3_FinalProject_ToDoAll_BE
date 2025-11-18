package com.server.search.controller;

import com.server.search.service.ResumeSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class ResumeIndexController {

    private final ResumeSearchService resumeSearchService;

    @PostMapping("/index/all")
    public String indexAllResumes() {
        resumeSearchService.indexAll(); // 전체 색인
        return "전체 이력서 색인 완료";
    }


    @GetMapping("/count")
    public String count() {
        long count = resumeSearchService.count();
        return "총 색인된 이력서 수: " + count;
    }
}

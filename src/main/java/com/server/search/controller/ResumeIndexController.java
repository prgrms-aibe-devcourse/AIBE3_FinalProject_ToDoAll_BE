package com.server.search.controller;

import com.server.search.document.ResumeDocument;
import com.server.search.service.ResumeSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/index/{resumeId}")
    public String indexOne(@PathVariable Long resumeId) {
        resumeSearchService.find(resumeId).ifPresentOrElse(
                doc -> resumeSearchService.index(doc.toEntity()),
                () -> System.out.println("이력서가 존재하지 않음")
        );
        return "개별 색인 시도 완료";
    }


    @GetMapping("/count")
    public String count() {
        long count = resumeSearchService.count();
        return "총 색인된 이력서 수: " + count;
    }

    @GetMapping("/indexed")
    public List<ResumeDocument> getAllIndexedResumes() {
        return resumeSearchService.findAllIndexedResumes();
    }
}

package com.server.search.service;

import com.server.resume.domain.Resume;
import com.server.search.document.ResumeDocument;
import com.server.search.repository.ResumeSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResumeSearchService {

    private final ResumeSearchRepository resumeSearchRepository;

    // 색인
    public void index(Resume resume) {
        ResumeDocument document = ResumeDocument.of(resume);
        resumeSearchRepository.save(document);
    }

    // 삭제
    public void delete(Long resumeId) {
        resumeSearchRepository.deleteById(resumeId);
    }

    // 단건 조회
    public Optional<ResumeDocument> find(Long resumeId) {
        return resumeSearchRepository.findById(resumeId);
    }
}
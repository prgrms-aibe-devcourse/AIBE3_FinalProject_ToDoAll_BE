package com.server.search.service;

import com.server.resume.domain.Resume;
import com.server.resume.repository.ResumeRepository;
import com.server.search.document.ResumeDocument;
import com.server.search.repository.ResumeSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResumeSearchService {

    private final ResumeSearchRepository resumeSearchRepository;
    private final ResumeRepository resumeRepository;

    // 개별 색인
    public void index(Resume resume) {
        ResumeDocument document = ResumeDocument.of(resume);
        resumeSearchRepository.save(document);
    }

    // 단건 조회
    public Optional<ResumeDocument> find(Long resumeId) {
        return resumeSearchRepository.findById(resumeId);
    }

    // 삭제
    public void delete(Long resumeId) {
        resumeSearchRepository.deleteById(resumeId);
    }

    // 전체 색인 기능
    public void indexAll() {
        List<Resume> resumes = resumeRepository.findAll();
        for (Resume resume : resumes) {
            ResumeDocument document = ResumeDocument.of(resume);
            resumeSearchRepository.save(document);
        }

        System.out.println(">> 색인 완료: 총 " + resumes.size() + "개");
    }

    public long count() {
        return resumeSearchRepository.count();
    }

    // 주석 풀면 서버 실행 시 자동 색인
    /*
    @PostConstruct
    public void init() {
        indexAll();
    }
    */
}
package com.server.search.service;

import com.server.resume.domain.Resume;
import com.server.resume.repository.ResumeRepository;
import com.server.search.document.ResumeDocument;
import com.server.search.repository.ResumeSearchRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResumeSearchService {

    private final ResumeSearchRepository resumeSearchRepository;
    private final ResumeRepository resumeRepository;

    // 개별 색인
    public void index(Resume resume) {
        // JD 또는 스킬 정보가 없는 이력서는 색인에서 제외
        if (resume.getJobDescription() == null || resume.getSkills().isEmpty()) {
            System.out.println("색인 제외: 이력서 ID " + resume.getId() + " (JD 또는 스킬 없음)");
            return;
        }
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
    @Transactional(readOnly = true)
    public void indexAll() {
        List<Resume> resumes = resumeRepository.findAll();
        for (Resume resume : resumes) {
            index(resume); // 위에서 조건 필터링 포함
        }

        System.out.println(">> 색인 완료: 총 " + resumes.size() + "개 시도 (실제 색인 개수는 로그 확인)");
    }

    public long count() {
        return resumeSearchRepository.count();
    }

    public List<ResumeDocument> findAllIndexedResumes() {
        return resumeSearchRepository.findAll();
    }

    // 주석 풀면 서버 실행 시 자동 색인
    /*
    @PostConstruct
    public void init() {
        indexAll();
    }
    */
}
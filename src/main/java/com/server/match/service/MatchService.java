package com.server.match.service;

import com.server.global.exception.ApplicationException;
import com.server.jd.domain.JobDescription;
import com.server.jd.repository.JobDescriptionRepository;
import com.server.match.domain.Match;
import com.server.match.domain.MatchStatus;
import com.server.match.dto.*;
import com.server.match.exception.MatchErrorCase;
import com.server.match.repository.MatchRepository;
import com.server.match.util.MatchScoreCalculator;
import com.server.match.util.RecommendationReasonBuilder;
import com.server.resume.domain.Resume;
import com.server.resume.repository.ResumeRepository;
import com.server.search.document.ResumeDocument;
import com.server.search.service.ResumeSearchService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final ResumeRepository resumeRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final ResumeSearchService resumeSearchService;
    private final ElasticsearchOperations elasticsearchOperations;


    @Transactional
    public Match registerMatch(MatchRequestDto dto) {
        JobDescription jd = jobDescriptionRepository.findById(dto.jdId())
                .orElseThrow(() -> new ApplicationException(MatchErrorCase.JD_NOT_FOUND));

        Resume resume = resumeRepository.findById(dto.resumeId())
                .orElseThrow(() -> new ApplicationException(MatchErrorCase.RESUME_NOT_FOUND));

        if (matchRepository.existsByJobDescription_IdAndResume_Id(jd.getId(), resume.getId())) {
            throw new ApplicationException(MatchErrorCase.MATCH_ALREADY_EXISTS);
        }

        Match match = Match.of(
                jd,
                resume,
                LocalDateTime.now(),
                null, // 현재 매칭 점수 없음 (추후 구현 후 계산)
                null, // 현재 추천 사유 없음
                MatchStatus.APPLIED // 기본 상태: 지원 완료
        );

        matchRepository.save(match);
        resumeSearchService.index(resume); // ES 색인 등록

        return match;
    }

    @Transactional(readOnly = true)
    public List<MatchListResponseDto> getMatchedResumes(MatchSearchCondition condition) {
        return matchRepository.searchMatches(condition).getContent();
    }

    @Transactional(readOnly = true)
    public MatchDetailResponseDto getMatchDetail(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ApplicationException(MatchErrorCase.MATCH_NOT_FOUND));

        String skillMatchRate = "78%"; // 향후 ES 도입 시 동적 계산
        List<String> missingSkills = List.of("Redis", "Kafka"); // 임시 데이터
        String recommendationReason = match.getRecommendationReason() != null
                ? match.getRecommendationReason()
                : "추천 사유가 아직 등록되지 않았습니다.";
        String resumeSummary = null;  // 향후 AI 요약 도입 전까지는 null
        String jdSummary = null;      // 향후 AI 요약 도입 전까지는 null

        return MatchDetailResponseDto.builder()
                .jdTitle(match.getJobDescription().getTitle())
                .resumeName(match.getResume().getName())
                .matchScore(match.getMatchScore() != null ? match.getMatchScore() : 0.0f)
                .skillMatchRate(skillMatchRate)
                .missingSkills(missingSkills)
                .recommendationReason(recommendationReason)
                .resumeSummary(resumeSummary)
                .jdSummary(jdSummary)
                .build();
    }

    @Transactional
    public MatchResponseDto updateMatchStatus(Long matchId, MatchStatus newStatus) {
        if (newStatus == null) {
            throw new ApplicationException(MatchErrorCase.MATCH_INVALID_STATUS);
        }

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ApplicationException(MatchErrorCase.MATCH_NOT_FOUND));

        match.updateStatus(newStatus);

        return new MatchResponseDto(match.getId(), match.getStatus());
    }

    @Transactional
    public List<ResumeDocument> recommendResumes(Long jdId) {

        JobDescription jd = jobDescriptionRepository.findById(jdId)
                .orElseThrow(() -> new ApplicationException(MatchErrorCase.JD_NOT_FOUND));

        // Elasticsearch 검색
        Criteria criteria = new Criteria("fullText").matches(jd.getDescription());
        // 페이징 (현재 0~10)
        Query query = new CriteriaQuery(criteria, PageRequest.of(0, 10));
        SearchHits<ResumeDocument> hits = elasticsearchOperations.search(query, ResumeDocument.class);

        List<ResumeDocument> recommended = hits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .toList();

        // 매칭된 이력서들에 대해 Match 객체 자동 생성
        for (ResumeDocument doc : recommended) {

            // 이미 매칭된 경우는 제외
            boolean alreadyExists = matchRepository.existsByJobDescription_IdAndResume_Id(jd.getId(), doc.getId());
            if (alreadyExists) continue;

            // 이력서 원본 조회
            Resume resume = resumeRepository.findById(doc.getId())
                    .orElse(null);

            if (resume == null) continue;

            // 점수 계산
            float score = MatchScoreCalculator.calculateMatchScore(jd.getDescription(), doc);

            // 추천 사유 생성
            String reason = RecommendationReasonBuilder.buildReason(jd.getDescription(), doc);

            // Match 객체 생성
            Match match = Match.of(
                    jd,
                    resume,
                    LocalDateTime.now(),
                    score,
                    reason,
                    MatchStatus.RECOMMENDED
            );

            matchRepository.save(match);
        }

        return recommended;
    }
}
package com.server.match.service;


import com.server.ai.service.KeywordExtractorService;
import com.server.global.exception.ApplicationException;
import com.server.jd.domain.JobDescription;
import com.server.jd.repository.JobDescriptionRepository;
import com.server.match.async.RecommendationAsyncService;
import com.server.match.cache.RedisRecommendationCacheService;
import com.server.match.domain.Match;
import com.server.match.domain.MatchStatus;
import com.server.match.dto.*;
import com.server.match.exception.MatchErrorCase;
import com.server.match.repository.MatchRepository;
import com.server.match.util.MatchScoreCalculator;
import com.server.resume.domain.Resume;
import com.server.resume.repository.ResumeRepository;
import com.server.search.document.ResumeDocument;
import com.server.search.dto.ResumeRecommendationDto;
import com.server.search.service.ResumeSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final ResumeRepository resumeRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final ResumeSearchService resumeSearchService;
    private final KeywordExtractorService keywordExtractorService;
    private final RedisRecommendationCacheService redisRecommendationCacheService;
    private final RecommendationAsyncService recommendationAsyncService;
    private final RecommendationCoreService recommendationCoreService;


    // JD 지원 + 매칭 등록
    @Transactional
    public Match applyToJobDescription(MatchRequestDto dto) {
        JobDescription jd = jobDescriptionRepository.findById(dto.jdId())
                .orElseThrow(() -> new ApplicationException(MatchErrorCase.JD_NOT_FOUND));

        Resume resume = resumeRepository.findById(dto.resumeId())
                .orElseThrow(() -> new ApplicationException(MatchErrorCase.RESUME_NOT_FOUND));

        if (matchRepository.existsByJobDescription_IdAndResume_Id(jd.getId(), resume.getId())) {
            throw new ApplicationException(MatchErrorCase.MATCH_ALREADY_EXISTS);
        }

        Match match = Match.ofForApplication(jd, resume);
        matchRepository.save(match);

        return match;
    }

    // JD 기반 추천 이력서 자동 매칭
    @Transactional
    public List<ResumeRecommendationDto> recommendResumes(Long jdId, Integer limit, String sortType) throws IOException {
        // 유효한 limit 값만 허용 (3, 5, 10, 20, 30) → 잘못 들어오면 기본값 10
        List<Integer> allowedLimits = List.of(3, 5, 10, 20, 30);
        if (!allowedLimits.contains(limit)) {
            limit = 10;
        }

        List<ResumeRecommendationDto> fullList;

        // 캐시 있으면 바로 반환
        if (redisRecommendationCacheService.existsRecommendationFor(jdId)) {
            log.info("[추천 캐시 HIT] JD {} — Redis에서 즉시 반환", jdId);
            fullList = redisRecommendationCacheService.getRecommendations(jdId);
        } else {
            // 캐시 없으면 바로 추천 계산 (동기)
            fullList = recommendationCoreService.calculateRecommendations(jdId);
            // 캐시 저장
            redisRecommendationCacheService.saveRecommendations(jdId, fullList);
            // 비동기로 다시 캐싱 예약
            recommendationAsyncService.warmUpRecommendation(jdId);
        }

        // 상위 limit 개수만 추려냄
        List<ResumeRecommendationDto> limitedList = fullList.stream()
                .limit(limit)
                .toList();
        
        //정렬
        if(sortType.equals("LATEST")) {
            return limitedList.stream()
                    .sorted(Comparator.comparing(ResumeRecommendationDto::resumeCreateTime).reversed())
                    .toList();
        } else {
            return limitedList.stream()
                    .sorted(Comparator.comparing(ResumeRecommendationDto::matchScore).reversed())
                    .toList();
        }
    }

    @Transactional(readOnly = true)
    public Page<MatchListResponseDto> getMatchedResumesPaged(MatchSearchCondition condition, Pageable pageable) {
        return matchRepository.searchMatches(condition, pageable);
    }

    @Transactional(readOnly = true)
    public MatchDetailResponseDto getMatchDetail(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ApplicationException(MatchErrorCase.MATCH_NOT_FOUND));

        Resume resume = match.getResume();
        ResumeDocument doc = resumeSearchService.find(resume.getId())
                .orElseGet(() -> ResumeDocument.of(resume));

        JobDescription jd = match.getJobDescription();

        // JD 키워드 추출
        List<String> jdKeywords = keywordExtractorService.extractKeywords(jd.getDescription());

        // 누락된 스킬
        List<String> missingSkills = MatchScoreCalculator.getMissingSkills(jd, doc);

        // 전체 기술 수
        int totalSkills = missingSkills.size() + doc.getSkills().size();
        float percentage = totalSkills > 0
                ? (float) (totalSkills - missingSkills.size()) / totalSkills
                : 0f;

        String skillMatchRate = Math.round(percentage * 100) + "%";

        return MatchDetailResponseDto.builder()
                .jdTitle(jd.getTitle())
                .resumeName(resume.getName())
                .matchScore(match.getMatchScore() != null ? match.getMatchScore() : 0.0f)
                .skillMatchRate(skillMatchRate)
                .missingSkills(missingSkills)
                .recommendationReason(match.getRecommendationReason())
                .resumeSummary(match.getResumeSummary())
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
    public MatchCancelResponseDto cancelMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ApplicationException(MatchErrorCase.MATCH_NOT_FOUND));

        String jdTitle = match.getJobDescription().getTitle();
        String resumeName = match.getResume().getName();

        matchRepository.delete(match);

        return new MatchCancelResponseDto(match.getId(), jdTitle, resumeName);
    }

    @Transactional
    public void checkMatch(MatchRequestDto dto) {
        JobDescription jd = jobDescriptionRepository.findById(dto.jdId())
                .orElseThrow(() -> new ApplicationException(MatchErrorCase.JD_NOT_FOUND));

        Resume resume = resumeRepository.findById(dto.resumeId())
                .orElseThrow(() -> new ApplicationException(MatchErrorCase.RESUME_NOT_FOUND));

        Match match = matchRepository.findByJobDescription_IdAndResume_Id(jd.getId(), resume.getId())
                .orElseThrow(() -> new ApplicationException(MatchErrorCase.MATCH_NOT_FOUND));

        // 이미 확정된 지원자면 중복 확정 불가
        if (match.getStatus() == MatchStatus.CONFIRMED) {
            throw new ApplicationException(MatchErrorCase.MATCH_ALREADY_CONFIRMED);
        }

        // 거절,보류된 경우 확정 불가
        if (match.getStatus() == MatchStatus.REJECTED || match.getStatus() == MatchStatus.HOLD) {
            throw new ApplicationException(MatchErrorCase.MATCH_CANNOT_BE_CONFIRMED);
        }
    }
}
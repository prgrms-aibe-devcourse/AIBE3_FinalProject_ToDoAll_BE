package com.server.match.service;

import com.server.global.exception.ApplicationException;
import com.server.jd.domain.JobDescription;
import com.server.jd.repository.JobDescriptionRepository;
import com.server.match.domain.Match;
import com.server.match.domain.MatchStatus;
import com.server.match.dto.MatchDetailResponseDto;
import com.server.match.dto.MatchListResponseDto;
import com.server.match.dto.MatchRequestDto;
import com.server.match.dto.MatchSearchCondition;
import com.server.match.exception.MatchErrorCase;
import com.server.match.repository.MatchRepository;
import com.server.resume.domain.Resume;
import com.server.resume.repository.ResumeRepository;
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
                null,
                MatchStatus.APPLIED // 기본 상태: 지원 완료
        );

        return matchRepository.save(match);
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
}
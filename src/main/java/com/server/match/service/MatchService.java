package com.server.match.service;

import com.server.global.exception.ApplicationException;
import com.server.jd.domain.JobDescription;
import com.server.jd.repository.JobDescriptionRepository;
import com.server.match.domain.Match;
import com.server.match.domain.MatchStatus;
import com.server.match.dto.MatchRequestDto;
import com.server.match.exception.MatchErrorCase;
import com.server.match.repository.MatchRepository;
import com.server.resume.domain.Resume;
import com.server.resume.repository.ResumeRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

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
                MatchStatus.APPLIED // 기본 상태: 지원 완료
        );

        return matchRepository.save(match);
    }
}
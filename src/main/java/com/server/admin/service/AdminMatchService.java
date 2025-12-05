package com.server.admin.service;

import com.server.admin.dto.AdminMatchDetailDto;
import com.server.admin.dto.AdminMatchListDto;
import com.server.global.exception.ApplicationException;
import com.server.match.domain.Match;
import com.server.match.domain.MatchStatus;
import com.server.match.dto.MatchDetailResponseDto;
import com.server.match.exception.MatchErrorCase;
import com.server.match.repository.MatchRepository;
import com.server.match.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMatchService {

    private final MatchRepository matchRepository;
    private final MatchService matchService;

    @Transactional(readOnly = true)
    public List<AdminMatchListDto> getAllMatches() {
        return matchRepository.findAllByOrderByAppliedAtDesc()
                .stream()
                .map(AdminMatchListDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminMatchDetailDto getDetail(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ApplicationException(MatchErrorCase.MATCH_NOT_FOUND));

        // 기존 MatchService의 상세 정보 (스킬 매칭률, 부족 스킬, 요약 등)
        MatchDetailResponseDto detail = matchService.getMatchDetail(matchId);

        return AdminMatchDetailDto.of(match, detail);
    }

    @Transactional
    public void updateStatus(Long matchId, MatchStatus status) {
        if (status == null) {
            throw new ApplicationException(MatchErrorCase.MATCH_INVALID_STATUS);
        }

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ApplicationException(MatchErrorCase.MATCH_NOT_FOUND));

        match.updateStatus(status);
    }

    @Transactional
    public void softDelete(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ApplicationException(MatchErrorCase.MATCH_NOT_FOUND));
        match.softDelete();
    }

    @Transactional
    public void restore(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ApplicationException(MatchErrorCase.MATCH_NOT_FOUND));
        match.restore();
    }
}

package com.server.match.port;


import com.server.match.dto.MatchResultDto;

// 매칭 엔진 포트 - 외부 연동 추상화
public interface MatchingEnginePort {
    MatchResultDto match(Long jdId);
}

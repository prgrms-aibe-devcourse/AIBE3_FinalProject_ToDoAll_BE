package com.server.match.adapter;

import com.server.match.dto.MatchResultDto;
import com.server.match.port.MatchingEnginePort;
import org.springframework.stereotype.Component;

import java.util.List;

// 실제 ElasticSearch 매칭 엔진을 호출하는 구현체
@Component
public class MatchingEngineAdapter implements MatchingEnginePort {

    @Override
    public MatchResultDto match(Long jdId) {
        // TODO: ElasticSearch 연동 및 매칭 결과 반환
        return MatchResultDto.builder()
                .jdId(jdId)
                .matchedResumeIds(List.of()) // 임시로 일단 빈 리스트
                .build();
    }
}

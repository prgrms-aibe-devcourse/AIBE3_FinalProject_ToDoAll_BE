package com.server.match.domain;

public enum MatchStatus {
    APPLIED,        // 지원됨
    RECOMMENDED,    // 추천됨
    CONFIRMED,
    BOOKMARK,       // 북마크됨
    HOLD,           // 보류됨
    REJECTED        // 거절됨
}
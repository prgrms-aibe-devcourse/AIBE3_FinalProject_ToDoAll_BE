package com.server.search.exception;

import com.server.global.exception.ErrorCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum SearchErrorCase implements ErrorCase {

    INVALID_RECOMMENDATION_ID(HttpStatus.BAD_REQUEST, 14001, "jdId와 resumeId는 필수입니다."),
    INVALID_MATCH_SCORE(HttpStatus.BAD_REQUEST, 14002, "matchScore는 0 ~ 100 사이여야 합니다."),
    SUMMARY_REQUIRED(HttpStatus.BAD_REQUEST, 14003, "요약(summary)은 비어 있을 수 없습니다."),
    RECOMMENDATION_REASON_REQUIRED(HttpStatus.BAD_REQUEST, 14004, "추천 사유는 비어 있을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final Integer errorCode;
    private final String message;

    @Override
    public Integer getHttpStatusCode() {
        return httpStatus.value();
    }

    @Override
    public Integer getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

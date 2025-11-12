package com.server.match.exception;

import com.server.global.exception.ErrorCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum MatchErrorCase implements ErrorCase {

    MATCH_NOT_FOUND(HttpStatus.NOT_FOUND, 8001, "해당 매칭 정보를 찾을 수 없습니다."),
    MATCH_ALREADY_EXISTS(HttpStatus.CONFLICT, 8002, "이미 지원한 이력서입니다."),
    MATCH_INVALID_STATUS(HttpStatus.BAD_REQUEST, 8003, "유효하지 않은 매칭 상태입니다.");

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
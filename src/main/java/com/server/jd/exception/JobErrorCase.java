package com.server.jd.exception;

import com.server.global.exception.ErrorCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum JobErrorCase implements ErrorCase {

    JOB_NOT_FOUND(HttpStatus.NOT_FOUND, 6001, "해당 채용공고를 찾을 수 없습니다."),
    INVALID_JOB_STATUS(HttpStatus.BAD_REQUEST, 6002, "유효하지 않은 공고 상태입니다.");

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

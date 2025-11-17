package com.server.interview.exception;

import com.server.global.exception.ErrorCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum InterviewErrorCase implements ErrorCase {

    INTERVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, 9001, "해당 인터뷰를 찾을 수 없습니다."),
    INTERVIEW_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, 9002, "면접을 삭제할 권한이 없습니다."),
    INVALID_STATUS(HttpStatus.BAD_REQUEST, 9003, "올바르지 않은 면접 상태 값입니다.");

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

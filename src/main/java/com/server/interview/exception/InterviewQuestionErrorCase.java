package com.server.interview.exception;

import com.server.global.exception.ErrorCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum InterviewQuestionErrorCase implements ErrorCase {
    INVALID_FIELD(HttpStatus.BAD_REQUEST, 9101, "질문 유형(type) 또는 질문 내용(contents)을 입력해주세요."),
    INVALID_FORMAT(HttpStatus.BAD_REQUEST, 9102, "요청 형식이 올바르지 않습니다. (contents는 배열이어야 합니다)"),
    FORBIDDEN(HttpStatus.FORBIDDEN, 9103, "면접에 질문을 등록할 권한이 없습니다.");

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

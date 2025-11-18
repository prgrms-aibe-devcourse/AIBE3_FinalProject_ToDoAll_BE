package com.server.interview.exception;

import com.server.global.exception.ErrorCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum InterviewEvaluationErrorCase implements ErrorCase {
    INTERVIEW_EVALUATION_NOT_FOUND(HttpStatus.NOT_FOUND, 9401, "해당 메모를 찾을 수 없습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, 9402, "해당 평가에 접근 권한이 없습니다."),
    EXIST_EVALUATION(HttpStatus.CONFLICT,9403 , "해당 인터뷰의 평가는 이미 존재합니다."),
    RESULT_REQUIRED(HttpStatus.BAD_REQUEST, 9404, "평가 결과(result)는 필수 입력 값입니다."),
    INVALID_RESULT(HttpStatus.BAD_REQUEST, 9405, "유효하지 않은 평가 결과(result) 값입니다. (PASS, HOLD, FAIL)");

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

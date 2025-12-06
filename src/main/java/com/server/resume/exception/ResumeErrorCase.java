package com.server.resume.exception;

import com.server.global.exception.ErrorCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ResumeErrorCase implements ErrorCase {

    RESUME_NOT_FOUND(HttpStatus.NOT_FOUND, 4041, "해당 이력서를 찾을 수 없습니다."),
    RESUME_OWNER_MISMATCH(HttpStatus.FORBIDDEN, 4042, "이력서 소유자가 아닙니다."),
    JD_NOT_FOUND(HttpStatus.NOT_FOUND, 4043, "해당 공고를 찾을 수 없습니다."),
    SKILL_NOT_FOUND(HttpStatus.NOT_FOUND, 4044, "해당 스킬을 찾을 수 없습니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, 4045, "이력서 입력값이 유효하지 않습니다."),
    INVALID_STATUS(HttpStatus.BAD_REQUEST, 4046, "유효하지 않은 상태 변경입니다."),
    INVALID_DATA(HttpStatus.BAD_REQUEST, 4047, "이력서 데이터 형식이 올바르지 않습니다."),;

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

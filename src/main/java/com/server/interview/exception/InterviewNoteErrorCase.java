package com.server.interview.exception;

import com.server.global.exception.ErrorCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum InterviewNoteErrorCase implements ErrorCase {
    FORBIDDEN(HttpStatus.FORBIDDEN, 9201, "면접 노트 접근 권한이 없습니다."),
    INTERVIEW_NOTE_NOT_FOUND(HttpStatus.NOT_FOUND, 9202, "해당 노트를 찾을 수 없습니다.");

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

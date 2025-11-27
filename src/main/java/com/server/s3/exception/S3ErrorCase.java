package com.server.s3.exception;

import com.server.global.exception.ErrorCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum S3ErrorCase implements ErrorCase {
    FILENAME_NOT_FOUND(HttpStatus.BAD_REQUEST, 10001, "업로드한 파일에 파일명이 없습니다."),
    FILE_UPLOAD_FAIL(HttpStatus.BAD_REQUEST, 10001, "S3에 파일을 업로드 하는 중 문제가 발생했습니다."),
    INVALID_FILE_KEY(HttpStatus.BAD_REQUEST, 10001, "잘못된 파일키입니다.");

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

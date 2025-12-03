package com.server.interview.exception;

import com.server.global.exception.ErrorCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum InterviewInviteTokenErrorCase implements ErrorCase {

    INVALID_TOKEN(HttpStatus.BAD_REQUEST, 9521, "초대 토큰이 올바르지 않습니다."),
    INVITE_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, 9522, "초대 토큰이 만료되었거나 존재하지 않습니다."),
    INVITE_TOKEN_PAYLOAD_INVALID(HttpStatus.BAD_REQUEST, 9523, "초대 토큰 정보가 손상되었습니다."),
    INVITE_TOKEN_MISMATCH(HttpStatus.FORBIDDEN, 9524, "해당 면접에 대한 초대 토큰이 아닙니다."),

    INVITE_TOKEN_SERIALIZE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 9525, "초대 토큰 생성에 실패했습니다."),
    INVITE_TOKEN_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 9526, "초대 토큰 삭제에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final Integer errorCode;
    private final String message;

    @Override public Integer getHttpStatusCode() { return httpStatus.value(); }
    @Override public Integer getErrorCode() { return errorCode; }
    @Override public String getMessage() { return message; }
}

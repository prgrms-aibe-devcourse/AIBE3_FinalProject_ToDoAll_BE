package com.server.user.exception;

import com.server.global.exception.ErrorCase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCase implements ErrorCase {



    // 404
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 1003, "사용자를 찾을 수 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 4011, "로그인이 필요합니다."),


    // 409 - 중복/충돌
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, 1001, "이미 가입된 이메일입니다."),
    PASSWORD_CONFIRM_MISMATCH(HttpStatus.CONFLICT, 1004, "비밀번호가 일치하지 않습니다."),

    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, 4012, "현재 비밀번호가 일치하지 않습니다."),
    PASSWORD_SAME_AS_OLD(HttpStatus.CONFLICT, 4093, "새 비밀번호는 이전 비밀번호와 같을 수 없습니다."),


    // 400 - 요청 형식/정책 위반

    USER_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, 3001, "요청 데이터 형식이 올바르지 않습니다."),
    PASSWORD_EQUALS_EMAIL_ID(HttpStatus.BAD_REQUEST, 3003, "비밀번호에 이메일 아이디를 그대로 사용할 수 없습니다."),


    // 400 - 허용되지 않은 성별
    INVALID_GENDER(HttpStatus.BAD_REQUEST, 4003, "허용되지 않는 성별 값입니다."),

    INVALID_PROFILE_IMAGE(HttpStatus.BAD_REQUEST, 3005, "유효하지 않은 프로필 이미지 파일입니다.");




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

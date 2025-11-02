package com.server.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.server.global.exception.ErrorCase;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final Integer errorCode;
    private final String message;
    private final T data;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .message("success")
                .data(data)
                .build();
    }

    public static ApiResponse<?> success() {
        return ApiResponse.builder()
                .message("success")
                .build();
    }

    public static ApiResponse<?> error(ErrorCase errorCase) {
        return ApiResponse.builder()
                .errorCode(errorCase.getErrorCode())
                .message(errorCase.getMessage())
                .build();
    }

    public static ApiResponse<?> error(int errorCode, String message) {
        return ApiResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .build();
    }
}

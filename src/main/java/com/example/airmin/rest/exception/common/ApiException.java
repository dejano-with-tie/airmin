package com.example.airmin.rest.exception.common;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private final ApiErrorCode errorCode;

    public ApiException(final String message, final ApiErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApiException(final String message, final ApiErrorCode errorCode, final Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}

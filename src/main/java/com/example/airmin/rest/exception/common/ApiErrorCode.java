package com.example.airmin.rest.exception.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ApiErrorCode {
    UNKNOWN(HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_FOUND(HttpStatus.NOT_FOUND),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    NOT_UNIQUE(HttpStatus.UNPROCESSABLE_ENTITY),
    RELATION_NOT_FOUND(HttpStatus.UNPROCESSABLE_ENTITY),
    CONSTRAINT_VALIDATION(HttpStatus.BAD_REQUEST),
    ;

    private final HttpStatus httpStatus;

    ApiErrorCode(final HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}

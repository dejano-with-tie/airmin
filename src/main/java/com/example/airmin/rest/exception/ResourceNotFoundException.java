package com.example.airmin.rest.exception;

import com.example.airmin.rest.exception.common.ApiErrorCode;
import com.example.airmin.rest.exception.common.ApiException;

public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(final String message) {
        super(message, ApiErrorCode.NOT_FOUND);
    }

    public ResourceNotFoundException(final String message, final ApiErrorCode errorCode) {
        super(message, errorCode);
    }

    public ResourceNotFoundException(final String message, final ApiErrorCode errorCode, final Throwable cause) {
        super(message, errorCode, cause);
    }
}

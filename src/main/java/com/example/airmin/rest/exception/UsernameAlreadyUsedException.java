package com.example.airmin.rest.exception;

import com.example.airmin.rest.exception.common.ApiErrorCode;
import com.example.airmin.rest.exception.common.ApiException;

public class UsernameAlreadyUsedException extends ApiException {

    public UsernameAlreadyUsedException(final String username) {
        super(String.format("Username '%s' already in use", username), ApiErrorCode.NOT_UNIQUE);
    }
}

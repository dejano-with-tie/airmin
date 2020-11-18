package com.example.airmin.rest.exception;

import com.example.airmin.rest.exception.common.ApiErrorCode;
import com.example.airmin.rest.exception.common.ApiException;

public class Unauthorized extends ApiException {

    public Unauthorized() {
        super("Unauthorized", ApiErrorCode.UNAUTHORIZED);
    }
}

package com.example.airmin.rest.exception;

import com.example.airmin.rest.exception.common.ApiErrorCode;
import com.example.airmin.rest.exception.common.ApiException;

public class CityAlreadyExistException extends ApiException {

    public CityAlreadyExistException(final String name, final String country) {
        super(String.format("City '[name=%s, country=%s]' already exist", name, country), ApiErrorCode.NOT_UNIQUE);
    }

}

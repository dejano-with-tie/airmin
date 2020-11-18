package com.example.airmin.rest.exception.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ApiErrorMessage {

    private String message;
    private ApiErrorCode code;
    private LocalDateTime timestamp = LocalDateTime.now();
    private List<ValidationError> errors;


    public ApiErrorMessage(String message) {
        this.message = message;
        this.code = ApiErrorCode.UNKNOWN;
    }

    public ApiErrorMessage(String message, ApiErrorCode code) {
        this.message = message;
        this.code = code;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String code;
        private String message;
        private String rejectedValue;
    }

}

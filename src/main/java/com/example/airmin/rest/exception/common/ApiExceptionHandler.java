package com.example.airmin.rest.exception.common;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Api exception handler
 */
@ControllerAdvice
@Log4j2
public class ApiExceptionHandler {

    @ExceptionHandler({
            ApiException.class
    })
    ResponseEntity<ApiErrorMessage> apiError(ApiException e) {
        final ApiErrorCode errorCode = e.getErrorCode();
        if (errorCode.equals(ApiErrorCode.NOT_FOUND)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(new ApiErrorMessage(e.getMessage(), errorCode));
    }

    @ExceptionHandler({
            AccessDeniedException.class
    })
    ResponseEntity<ApiErrorMessage> accessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    ResponseEntity<ApiErrorMessage> constrainsError(MethodArgumentNotValidException e) {
        final ApiErrorCode errorCode = ApiErrorCode.CONSTRAINT_VALIDATION;
        final ApiErrorMessage body = new ApiErrorMessage("Validation error", errorCode
        );

        final var errors = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError ->
                        new ApiErrorMessage.ValidationError(fieldError.getField(), fieldError.getCode(),
                                fieldError.getDefaultMessage(), Objects.toString(fieldError.getRejectedValue())))
                .sorted((o1, o2) -> o1.getField().compareToIgnoreCase(o2.getField()))
                .collect(Collectors.toList());
        body.setErrors(errors);

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(body);
    }
}

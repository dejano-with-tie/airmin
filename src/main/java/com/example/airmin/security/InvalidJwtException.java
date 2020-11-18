package com.example.airmin.security;

import org.springframework.security.core.AuthenticationException;

public class InvalidJwtException extends AuthenticationException {

    public InvalidJwtException(final String msg, final Throwable t) {
        super(msg, t);
    }

    public InvalidJwtException(final String msg) {
        super(msg);
    }
}

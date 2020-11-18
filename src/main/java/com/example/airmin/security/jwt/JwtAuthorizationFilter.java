package com.example.airmin.security.jwt;

import io.jsonwebtoken.JwtException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Log4j2
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    final JwtUtil jwtUtil;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, final JwtUtil jwtUtil) {
        super(authenticationManager);
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException, ServletException {
        logger.info(request.getRequestURL().toString());
        retrieveToken(request).ifPresent(this::authenticate);
        chain.doFilter(request, response);
    }

    private void authenticate(final String token) {
        try {
            SecurityContextHolder.getContext().setAuthentication(jwtUtil.parse(token));
        } catch (JwtException e) {
            log.error("Failed to parse jwt");
            log.trace(e.getMessage(), e);
        }
    }

    private Optional<String> retrieveToken(final HttpServletRequest httpRequest) {
        final var authorizationHeader = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith(JwtUtil.JWT_PREFIX)) {
            return Optional.of(authorizationHeader.substring(JwtUtil.JWT_PREFIX.length()));
        }
        return Optional.empty();
    }

}

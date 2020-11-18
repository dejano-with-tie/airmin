package com.example.airmin.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Log4j2
public class JwtUtil {

    public static final String JWT_PREFIX = "Bearer ";
    public static final String ROLES_KEY = "roles";

    private final JwtProperties jwtProperties;
    private JwtParser jwtParser;
    private SecretKey key;

    public JwtUtil(final JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    public void postConstruct() {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecretString().getBytes(StandardCharsets.UTF_8));
        this.jwtParser = Jwts.parserBuilder().setSigningKey(this.key).build();
    }

    public Authentication parse(@NonNull final String token) {
        final Claims claims = jwtParser.parseClaimsJws(token).getBody();
        final var roles = Arrays.stream(claims.get(ROLES_KEY).toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
        return new UsernamePasswordAuthenticationToken(claims.getSubject(), null, roles);
    }

    public String createToken(@NonNull Authentication authentication) {
        Claims claims = Jwts.claims().setSubject(authentication.getName());
        claims.put(ROLES_KEY, authentication.getAuthorities().stream().map(Object::toString)
                .collect(Collectors.joining(",")));

        return JWT_PREFIX + Jwts.builder()
                .setClaims(claims)
                .signWith(this.key)
                .setExpiration(new Date(System.currentTimeMillis() + TimeUnit.MINUTES
                        .toMillis(jwtProperties.getDurationInMinutes())))
                .compact();
    }
}

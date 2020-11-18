package com.example.airmin.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties("app.security.jwt")
@Getter
@AllArgsConstructor
@ConstructorBinding
public class JwtProperties {

    public final String secretString;

    public final int durationInMinutes;
}


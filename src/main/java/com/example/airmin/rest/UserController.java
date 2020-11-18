package com.example.airmin.rest;

import com.example.airmin.model.User;
import com.example.airmin.rest.dto.CredentialsDto;
import com.example.airmin.rest.dto.SignupDto;
import com.example.airmin.rest.dto.UserDto;
import com.example.airmin.security.jwt.JwtUtil;
import com.example.airmin.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserService userService;
    private final ModelMapper modelMapper;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtUtil jwtUtil;

    public UserController(final UserService userService, final ModelMapper modelMapper, final AuthenticationManagerBuilder authenticationManagerBuilder, final JwtUtil jwtUtil) {
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Register new user
     *
     * @param signup holds information about the new user.
     */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserDto> signup(@Validated @RequestBody SignupDto signup) {
        return ResponseEntity
                .ok(modelMapper.map(userService.register(modelMapper.map(signup, User.class)), UserDto.class));
    }

    /**
     * Authenticate user
     *
     * @param credentials credentials
     * @return {@link ResponseEntity<Void>} with status {@link HttpStatus#OK} and additional header
     * {@link HttpHeaders#AUTHORIZATION} containing token
     */
    @PostMapping("/auth")
    public ResponseEntity<Void> auth(@Validated @RequestBody CredentialsDto credentials) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(credentials.getUsername(), credentials.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION, jwtUtil.createToken(authentication)).build();
    }

}

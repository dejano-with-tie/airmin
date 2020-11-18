package com.example.airmin.security;

import com.example.airmin.model.User;
import com.example.airmin.rest.exception.Unauthorized;
import com.example.airmin.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityUtil {


    private final UserService userService;

    public SecurityUtil(final UserService userService) {
        this.userService = userService;
    }

    /**
     * Get {@link User} by currently authenticated principal
     *
     * @return authenticated {@link User}
     */
    public User getUser() {
        return userService.find(Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .orElseThrow(Unauthorized::new).getName());
    }
}

package com.example.airmin.service;

import com.example.airmin.model.Role;
import com.example.airmin.model.User;
import com.example.airmin.repository.UserRepository;
import com.example.airmin.rest.exception.UsernameAlreadyUsedException;
import lombok.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityNotFoundException;
import java.util.Collections;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(final UserRepository userRepository, final PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User find(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(String
                        .format("User with '%s' username does not exist", username)));
    }

    public User register(@NonNull User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new UsernameAlreadyUsedException(user.getUsername());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (CollectionUtils.isEmpty(user.getRoles())) {
            user.setRoles(Collections.singleton(Role.ROLE_USER));
        }

        return userRepository.save(user);
    }
}

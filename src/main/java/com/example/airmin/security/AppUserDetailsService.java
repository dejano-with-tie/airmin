package com.example.airmin.security;

import com.example.airmin.model.Role;
import com.example.airmin.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override public UserDetails loadUserByUsername(final String username) {
        return userRepository.findByUsername(username)
                .map(user ->
                        new User(user.getUsername(), user.getPassword(), getAuthorities(user.getRoles())))
                .orElseThrow(() -> new UsernameNotFoundException(String
                        .format("Username '%s' doesn't exist", username)));
    }

    private List<SimpleGrantedAuthority> getAuthorities(final Set<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());
    }
}

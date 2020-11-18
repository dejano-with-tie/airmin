package com.example.airmin.repository;

import com.example.airmin.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface UserRepository extends PagingAndSortingRepository<User, Long> {

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByUsername(String username);
}

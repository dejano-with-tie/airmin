package com.example.airmin.repository;

import com.example.airmin.model.City;
import com.example.airmin.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CityCommentRepository extends JpaRepository<Comment, Long> {

    Optional<Comment> findByIdAndAuthorId(Long id, Long authorId);

    Page<Comment> findDistinctByCityIsIn(List<City> cities, Pageable pageable);
}

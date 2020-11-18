package com.example.airmin.repository;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.example.airmin.model.City;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CityRepository extends EntityGraphJpaRepository<City, Long> {

    Optional<City> findByNameIgnoreCaseAndCountryIgnoreCase(String name, String country);

    Page<City> findByNameIsContainingIgnoreCaseOrderById(String name, Pageable page);

    List<City> findDistinctByIdIn(Collection<Long> ids, Sort sort, EntityGraph graph);

}

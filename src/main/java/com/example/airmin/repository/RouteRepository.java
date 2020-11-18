package com.example.airmin.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.example.airmin.model.Route;

public interface RouteRepository extends EntityGraphJpaRepository<Route, Long> {
}

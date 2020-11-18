package com.example.airmin.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.example.airmin.model.Airport;
import com.example.airmin.model.City;

import java.util.List;
import java.util.Optional;

public interface AirportRepository extends EntityGraphJpaRepository<Airport, Long> {

    Optional<Airport> findByExternalId(Long externalId);

    List<Airport> findAllByCity(City city);
}

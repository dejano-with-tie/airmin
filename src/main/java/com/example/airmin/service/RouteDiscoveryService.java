package com.example.airmin.service;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.example.airmin.model.Airport;
import com.example.airmin.model.Airport_;
import com.example.airmin.model.City;
import com.example.airmin.model.Route;
import com.example.airmin.repository.AirportRepository;
import com.example.airmin.repository.CityRepository;
import com.example.airmin.rest.exception.ResourceNotFoundException;
import com.example.airmin.rest.exception.common.ApiErrorCode;
import com.example.airmin.service.shortestpath.Dijkstra;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@Log4j2
public class RouteDiscoveryService {

    private final CityRepository cityRepository;
    private final AirportRepository airportRepository;
    private final Dijkstra dijkstra;
    private final CacheManager cacheManager;

    public RouteDiscoveryService(final CityRepository cityRepository, final AirportRepository airportRepository,
                                 final Dijkstra dijkstra, final CacheManager cacheManager) {
        this.cityRepository = cityRepository;
        this.airportRepository = airportRepository;
        this.dijkstra = dijkstra;
        this.cacheManager = cacheManager;
    }

    /**
     * Find the cheapest route from {@code sourceCityId} to {@code destinationCityId}.
     * <p>
     * This is achieved by exploring the airports graph with Dijkstra algorithm. This could be improved by
     * implementing Bidirectional Dijkstra, or even better apply contraction hierarchies preprocessing
     *
     * @param sourceCityId      starting point
     * @param destinationCityId destination point
     * @return {@link List<Route>} list of routes to reach destination
     */
    public List<Route> cheapest(@NonNull Long sourceCityId, @NonNull Long destinationCityId) {
        final Iterable<Airport> airports = tryFromCache();

        final City sourceCity = cityRepository.findById(sourceCityId).orElseThrow(() ->
                new ResourceNotFoundException(String
                        .format("City with id '%s' doesn't exist", sourceCityId), ApiErrorCode.RELATION_NOT_FOUND));
        final City destinationCity = cityRepository.findById(destinationCityId).orElseThrow(() ->
                new ResourceNotFoundException(String
                        .format("City with id '%s' doesn't exist", destinationCityId), ApiErrorCode.RELATION_NOT_FOUND));

        List<Airport> sourceAirports = new ArrayList<>();
        List<Airport> destinationAirports = new ArrayList<>();
        for (final Airport airport : airports) {
            if (sourceCity.equals(airport.getCity())) {
                sourceAirports.add(airport);
            }
            if (destinationCity.equals(airport.getCity())) {
                destinationAirports.add(airport);
            }
        }

        final Map<Airport, Airport> path = dijkstra.calculateGraph(airports, sourceAirports, destinationAirports);
        return dijkstra.shortestPath(destinationAirports, path);
    }

    private Iterable<Airport> tryFromCache() {
        final Cache airportsCache = cacheManager.getCache(Airport.class.getSimpleName().toLowerCase());
        Objects.requireNonNull(airportsCache);
        final String cacheKey = "all";
        final Object airports = Optional.ofNullable(airportsCache.get(cacheKey)).map(Cache.ValueWrapper::get)
                .orElseGet(() -> airportRepository
                        .findAll(EntityGraphUtils.fromAttributePaths(Airport_.departures.getName(),
                                Airport_.city.getName())));
        airportsCache.putIfAbsent(cacheKey, airports);
        return (Iterable<Airport>) airports;
    }

}

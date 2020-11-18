package com.example.airmin.util;

import com.example.airmin.model.Airport;
import com.example.airmin.model.City;
import com.example.airmin.model.Route;
import com.example.airmin.repository.AirportRepository;
import com.example.airmin.repository.CityRepository;
import com.example.airmin.repository.RouteRepository;
import com.example.airmin.rest.dto.CityDto;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class DataTestUtil {

    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private AirportRepository airportRepository;
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private CacheManager cacheManager;

    public CityDto createDto(CityData city) {
        return new CityDto(city.name, city.country, city.description);
    }

    @Transactional
    public List<City> save(CityData[] cities) {
        return cityRepository.saveAll(Arrays.stream(cities).map(c -> new City(c.name, c.country, c.description))
                .collect(Collectors.toList()));
    }

    @Transactional
    public City saveIfNotExist(CityData city) {
        return cityRepository
                .findByNameIgnoreCaseAndCountryIgnoreCase(city.getName(), city.getCountry())
                .orElseGet(() -> save(city));
    }

    @Transactional
    public City save(CityData city) {
        return cityRepository
                .save(new City(city.name, city.country, city.description));
    }

    @Transactional
    public Route save(final AirportData source, final AirportData destination, final double price) {
        Objects.requireNonNull(cacheManager.getCache(Airport.class.getSimpleName().toLowerCase())).clear();
        final Airport sourceAirport = saveIfNotExist(source);
        final Airport destinationAirport = saveIfNotExist(destination);
        return routeRepository.save(new Route(price, sourceAirport, destinationAirport));
    }

    @Transactional
    public Airport saveIfNotExist(AirportData airportData) {
        Objects.requireNonNull(cacheManager.getCache(Airport.class.getSimpleName().toLowerCase())).clear();
        return airportRepository
                .findByExternalId(airportData.externalId)
                .orElseGet(() -> save(airportData));
    }

    @Transactional
    public Airport save(AirportData airportData) {
        Objects.requireNonNull(cacheManager.getCache(Airport.class.getSimpleName().toLowerCase())).clear();
        return airportRepository
                .save(new Airport(airportData.externalId, airportData.name, saveIfNotExist(airportData.cityData)));
    }

    @Transactional
    public void deleteAll() {
        routeRepository.deleteAll();
        airportRepository.deleteAll();
        cityRepository.deleteAll();
    }

    @Getter
    public enum AirportData {
        MOSTAR(1645L, "Mostar - Bosnia", CityData.MOSTAR),
        NEW_YORK(3797L, "New York - JFK", CityData.NEW_YORK),
        BELGRADE_TESLA(1739L, "Belgrade - Tesla", CityData.BELGRADE),
        BERLIN_TEGEL(351L, "Berlin - Tegel", CityData.BERLIN),
        PARIS(1380L, "Paris", CityData.PARIS),
        MOSCOW_SHEREMETYEVO(2985L, "Moscow - Sheremetyevo", CityData.MOSCOW),
        MOSCOW_VNUKOVO(2988L, "Moscow - Vnukovo", CityData.MOSCOW),
        ROME_LEONARDO(1555L, "Rome - Lonardo", CityData.ROME),
        MADAGASCAR(5615L, "Madagascar - Antsalova", CityData.ANTSALOVA),
        SINGAPORE(3315L, "Singapore - Singapore", CityData.SINGAPORE),
        AUSTRALIA(3334L, "Australia - Melbourne", CityData.MELBOURNE),
        ;

        private final Long externalId;
        private final String name;
        private final CityData cityData;

        AirportData(final Long externalId, final String name, CityData cityData) {
            this.externalId = externalId;
            this.name = name;
            this.cityData = cityData;
        }
    }

    @Getter
    public enum CityData {
        BELGRADE("Belgrade", "Serbia", "Capital of Serbia"),
        MOSTAR("Mostar", "Bosnia and Herzegovina", "Mostar is lovely"),
        BERLIN("Berlin", "Germany", "Capital"),
        STERLING("Sterling", "United States", "contains erlin"),
        NEW_YORK("New York", "United States", "rly big"),
        PARIS("Paris", "France", "Capital"),
        MOSCOW("Moscow", "Russia", "Capital"),
        ROME("Rome", "Italy", "Capital"),
        ANTSALOVA("Antsalova", "Madagascar", "Madagascar"),
        SINGAPORE("Singapore", "Singapore", "Singapore"),
        MELBOURNE("Melbourne", "Australia", "Melbourne"),
        KAIKOURA("Kaikoura", "New Zealand", "Kaikoura"),
        ;

        private final String name;
        private final String country;
        private final String description;

        CityData(final String name, final String country, final String description) {
            this.name = name;
            this.country = country;
            this.description = description;
        }
    }

}

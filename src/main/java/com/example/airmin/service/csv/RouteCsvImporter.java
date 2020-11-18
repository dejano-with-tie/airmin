package com.example.airmin.service.csv;

import com.example.airmin.model.Airport;
import com.example.airmin.model.Route;
import com.example.airmin.repository.AirportRepository;
import com.example.airmin.repository.RouteRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class RouteCsvImporter extends CsvImporter<Route> {

    private final AirportRepository airportRepository;
    private final RouteRepository routeRepository;

    public RouteCsvImporter(final AirportRepository airportRepository, final RouteRepository routeRepository,
                            final CSVFormat csvFormat, final CacheManager cacheManager) {
        super(csvFormat, cacheManager);
        this.airportRepository = airportRepository;
        this.routeRepository = routeRepository;
    }

    @Override protected List<Route> process(final Iterable<CSVRecord> records) {
        final List<Airport> airports = airportRepository.findAll();
        return StreamSupport.stream(records.spliterator(), false)
                .map(mapping(airports))
                .filter(this::requiredPropsNonNull)
                .filter(route -> !Objects.equals(route.getDestination().getId(), route.getSource().getId()))
                .collect(Collectors.toList());
    }

    protected Function<CSVRecord, Route> mapping(final List<Airport> airports) {
        return record -> {
            final Route route = new Route();
            route.setAirlineCode(get(RouteCsvColumns.AIRLINE_CODE, record));
            route.setAirlineId(get(RouteCsvColumns.AIRLINE_ID, record));
            route.setCodeShare(get(RouteCsvColumns.CODE_SHARE, record));
            route.setNumberOfStops(get(RouteCsvColumns.NUMBER_OF_STOPS, record));
            route.setEquipment(get(RouteCsvColumns.EQUIPMENT, record));
            route.setPrice(get(RouteCsvColumns.PRICE, record));

            route.setDestination(airports.stream()
                    .filter(a -> a.getExternalId().equals(get(RouteCsvColumns.DESTINATION_AIRPORT_ID,
                            record))).findFirst()
                    .orElse(null));
            route.setSource(airports.stream()
                    .filter(a -> a.getExternalId().equals(get(RouteCsvColumns.SOURCE_AIRPORT_ID,
                            record))).findFirst()
                    .orElse(null));

            return route;
        };
    }

    private boolean requiredPropsNonNull(final Route route) {
        return route.getDestination() != null && route.getSource() != null && route.getPrice() != null;
    }

    @Override protected void persist(final List<Route> items) {
        routeRepository.saveAll(items);
    }

    private boolean doesntExist(final Route existing, final List<Route> routes) {
        return routes.stream().noneMatch(r -> compare(existing, r));
    }

    private boolean compare(final Route route, final Route r) {
        return r.getSource().getExternalId().equals(route.getSource().getExternalId())
                && r.getDestination().getExternalId().equals(route.getDestination().getExternalId())
                && r.getPrice().equals(route.getPrice());
    }
}

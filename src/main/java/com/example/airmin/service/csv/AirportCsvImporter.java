package com.example.airmin.service.csv;

import com.example.airmin.model.Airport;
import com.example.airmin.model.City;
import com.example.airmin.repository.AirportRepository;
import com.example.airmin.repository.CityRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class AirportCsvImporter extends CsvImporter<Airport> {

    private final AirportRepository airportRepository;
    private final CityRepository cityRepository;

    public AirportCsvImporter(final CSVFormat csvFormat, final AirportRepository airportRepository,
                              final CityRepository cityRepository, final CacheManager cacheManager) {
        super(csvFormat, cacheManager);
        this.airportRepository = airportRepository;
        this.cityRepository = cityRepository;
    }

    @Override
    @Transactional
    protected List<Airport> process(final Iterable<CSVRecord> records) {
        final List<City> cities = cityRepository.findAll();
        final List<Airport> airports = airportRepository.findAll();
        return StreamSupport.stream(records.spliterator(), true)
                .map(mapping(cities))
                .filter(a -> a.getCity() != null)
                .filter(notExist(airports))
                .collect(Collectors.toList());
    }

    private Predicate<Airport> notExist(final List<Airport> airports) {
        return airport ->
                airports.stream().filter(a -> a.getExternalId().equals(airport.getExternalId())).findFirst().isEmpty();
    }

    protected Function<CSVRecord, Airport> mapping(final List<City> cities) {
        return record -> {
            final var airport = new Airport();
            airport.setExternalId(get(AirportCsvColumns.ID, record));
            airport.setName(get(AirportCsvColumns.NAME, record));
            airport.setIataCode(get(AirportCsvColumns.IATA_CODE, record));
            airport.setIcaoCode(get(AirportCsvColumns.ICAO_CODE, record));
            airport.setLatitude(get(AirportCsvColumns.LATITUDE, record));
            airport.setLongitude(get(AirportCsvColumns.LONGITUDE, record));
            airport.setAltitude(get(AirportCsvColumns.ALTITUDE, record));
            airport.setType(get(AirportCsvColumns.TYPE, record));
            airport.setZoneId(get(AirportCsvColumns.TIMEZONE, record));
            airport.setZoneOffset(get(AirportCsvColumns.TIMEZONE_OFFSET, record));
            airport.setDataSource(get(AirportCsvColumns.SOURCE, record));
            airport.setCity(Optional.<String>ofNullable(get(AirportCsvColumns.CITY, record))
                    .flatMap(cityName ->
                            cities.stream()
                                    .filter(c -> c.getName().equalsIgnoreCase(cityName) &&
                                            c.getCountry().equalsIgnoreCase(get(AirportCsvColumns.COUNTRY, record)))
                                    .findFirst())
                    .orElse(null)
            );
            return airport;
        };
    }

    @Transactional
    @Override
    protected void persist(final List<Airport> items) {
        airportRepository.saveAll(items);
    }

}

package com.example.airmin.service.csv;

import com.example.airmin.model.Airport;
import com.example.airmin.repository.AirportRepository;
import com.example.airmin.repository.RouteRepository;
import com.example.airmin.util.DataTestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@SpringBootTest
@ActiveProfiles("test")
class RouteCsvImporterTest {
    @Autowired
    private AirportCsvImporter airportImporter;
    @Autowired
    private RouteCsvImporter routeCsvImporter;
    @Autowired
    private AirportRepository airportRepository;
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private DataTestUtil dataTestUtil;

    @BeforeEach
    void setUp() throws FileNotFoundException {
        dataTestUtil.save(DataTestUtil.CityData.BERLIN);
        dataTestUtil.save(DataTestUtil.CityData.BELGRADE);
        dataTestUtil.save(DataTestUtil.CityData.STERLING);
        final File airportsFile = ResourceUtils.getFile("classpath:datasets/airports.csv");
        airportImporter.importData(airportsFile);
    }

    @AfterEach
    void tearDown() {
        dataTestUtil.deleteAll();
    }

    @Test
    void dontImportRouteWhenCityDoesntExist() throws FileNotFoundException {
        airportRepository.deleteAllInBatch();
        dataTestUtil.deleteAll();

        final File routesFile = ResourceUtils.getFile("classpath:datasets/routes.csv");
        routeCsvImporter.importData(routesFile);
        Assertions.assertEquals(0, airportRepository.count());
        airportImporter.importData(routesFile);
        Assertions.assertEquals(0, routeRepository.count());
    }

    @Test
    void importSuccessfully() throws IOException {
        final File routesFile = ResourceUtils.getFile("classpath:datasets/routes.csv");
        final List<Long> definedAirports = airportRepository.findAll().stream().map(Airport::getExternalId)
                .collect(Collectors.toList());
        final long expectedRoutes = StreamSupport
                .stream(routeCsvImporter.getCsvRecords(Files.readAllBytes(routesFile.toPath())).spliterator(), false)
                .filter(record -> {
                    final Long destinationId = routeCsvImporter.<Long>get(RouteCsvColumns.DESTINATION_AIRPORT_ID, record);
                    final Long sourceId = routeCsvImporter.<Long>get(RouteCsvColumns.SOURCE_AIRPORT_ID, record);
                    final Double price = routeCsvImporter.<Double>get(RouteCsvColumns.PRICE, record);
                    return definedAirports.contains(destinationId) && definedAirports
                            .contains(sourceId) && price != null;
                })
                .count();

        routeCsvImporter.importData(routesFile);
        Assertions.assertEquals(expectedRoutes, routeRepository.count());
    }
}
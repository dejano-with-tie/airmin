package com.example.airmin.service.csv;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.example.airmin.model.Airport;
import com.example.airmin.model.Airport_;
import com.example.airmin.repository.AirportRepository;
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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@SpringBootTest
@ActiveProfiles("test")
class AirportCsvImporterTest {

    @Autowired
    private AirportCsvImporter importer;
    @Autowired
    private AirportRepository airportRepository;
    @Autowired
    private DataTestUtil dataTestUtil;

    @BeforeEach
    void setUp() {
        dataTestUtil.save(DataTestUtil.CityData.BERLIN);
        dataTestUtil.save(DataTestUtil.CityData.BELGRADE);
        dataTestUtil.save(DataTestUtil.CityData.STERLING);
    }

    @AfterEach
    void tearDown() {
        dataTestUtil.deleteAll();
    }

    @Test
    void importSuccessfully() throws FileNotFoundException {
        // City(no of airports): berlin(3), belgrade(1), sterling(1)
        final File airportsFile = ResourceUtils.getFile("classpath:datasets/airports.csv");
        importer.importData(airportsFile);
        final List<Airport> imported =
                StreamSupport
                        .stream(airportRepository.findAll(EntityGraphUtils.fromAttributePaths(Airport_.city.getName()))
                                .spliterator(), false)
                        .collect(Collectors.toList());
        Assertions.assertEquals(5, imported.size());
        Assertions.assertEquals(3, count(imported, DataTestUtil.CityData.BERLIN));
        Assertions.assertEquals(1, count(imported, DataTestUtil.CityData.BELGRADE));
        Assertions.assertEquals(1, count(imported, DataTestUtil.CityData.STERLING));
    }

    private long count(final List<Airport> imported, final DataTestUtil.CityData berlin) {
        return imported.stream().filter(a -> a.getCity().getName().equalsIgnoreCase(berlin.getName())).count();
    }

    @Test
    void ignoreDuplicateImport() throws FileNotFoundException {
        // City(no of airports): berlin(3), belgrade(1), sterling(1)
        final File airportsFile = ResourceUtils.getFile("classpath:datasets/airports.csv");
        importer.importData(airportsFile);
        Assertions.assertEquals(5, airportRepository.count());
        importer.importData(airportsFile);
        Assertions.assertEquals(5, airportRepository.count());
    }
}
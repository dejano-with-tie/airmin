package com.example.airmin.service;

import com.example.airmin.model.City;
import com.example.airmin.model.Route;
import com.example.airmin.repository.CityRepository;
import com.example.airmin.service.csv.AirportCsvColumns;
import com.example.airmin.service.csv.AirportCsvImporter;
import com.example.airmin.service.csv.RouteCsvImporter;
import com.example.airmin.util.DataTestUtil;
import com.example.airmin.util.DataTestUtil.AirportData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RouteDiscoveryServiceTest {

    @Autowired
    private RouteDiscoveryService routeDiscoveryService;
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private AirportCsvImporter airportCsvImporter;
    @Autowired
    private RouteCsvImporter routeCsvImporter;
    @Autowired
    private DataTestUtil dataTestUtil;
    private City berlin;
    private City melbourne;
    private City mostar;
    private City newZealand;
    private City newYork;

    /**
     * Import all cities, airplanes, routes
     *
     * @throws IOException file read
     */
    @BeforeAll
    void beforeAll() throws IOException {
        final File airportsFile = ResourceUtils.getFile("classpath:datasets/airports.csv");
        final Set<City> cities =
                StreamSupport.stream(airportCsvImporter.getCsvRecords(Files.readAllBytes(airportsFile.toPath()))
                        .spliterator(), false)
                        .map(record -> {
                            final String city = airportCsvImporter.get(AirportCsvColumns.CITY, record);
                            final String country = airportCsvImporter.get(AirportCsvColumns.COUNTRY, record);
                            return new City(city, country, "lovely");
                        }).collect(Collectors.toSet());
        cityRepository.saveAll(cities);

        airportCsvImporter.importData(airportsFile);
        routeCsvImporter.importData(ResourceUtils.getFile("classpath:datasets/routes.csv"));

        berlin = cityRepository.findByNameIgnoreCaseAndCountryIgnoreCase(DataTestUtil.CityData.BERLIN.getName(),
                DataTestUtil.CityData.BERLIN.getCountry()).orElseThrow();
        melbourne = cityRepository.findByNameIgnoreCaseAndCountryIgnoreCase(DataTestUtil.CityData.MELBOURNE.getName(),
                DataTestUtil.CityData.MELBOURNE.getCountry()).orElseThrow();
        mostar = cityRepository.findByNameIgnoreCaseAndCountryIgnoreCase(DataTestUtil.CityData.MOSTAR.getName(),
                DataTestUtil.CityData.MOSTAR.getCountry()).orElseThrow();
        newZealand = cityRepository.findByNameIgnoreCaseAndCountryIgnoreCase(DataTestUtil.CityData.KAIKOURA.getName(),
                DataTestUtil.CityData.KAIKOURA.getCountry()).orElseThrow();
        newYork = cityRepository.findByNameIgnoreCaseAndCountryIgnoreCase(DataTestUtil.CityData.NEW_YORK.getName(),
                DataTestUtil.CityData.NEW_YORK.getCountry()).orElseThrow();
    }

    @AfterAll
    void afterAll() {
        dataTestUtil.deleteAll();
    }

    /**
     * 1. Find cheapest route from Berlin to Melbourne
     * 2. Define new route which total price is half the price of previous best
     * <p>
     * -> Expect new route to be cheapest
     */
    @Test
    void successfullyFindCheapestRoute() {

        final double cheapestRoutesPrice = routeDiscoveryService.cheapest(berlin.getId(), melbourne.getId())
                .stream().mapToDouble(Route::getPrice).sum();

        // berlin -> belgrade
        // belgrade -> moscow
        // moscow -> Antsalova (madagascar)
        // madagascar -> singapore
        // singapore -> Adelaide (australia)
        // half the price
        final double priceForEachRoute = (cheapestRoutesPrice / 2) / 5;
        final Route berlinBelgrade = dataTestUtil
                .save(AirportData.BERLIN_TEGEL, AirportData.BELGRADE_TESLA, priceForEachRoute);
        final Route belgradeMoscow = dataTestUtil
                .save(AirportData.BELGRADE_TESLA, AirportData.MOSCOW_SHEREMETYEVO, priceForEachRoute);
        final Route moscowMadagascar = dataTestUtil
                .save(AirportData.MOSCOW_SHEREMETYEVO, AirportData.MADAGASCAR, priceForEachRoute);
        final Route madagascarSingapore = dataTestUtil
                .save(AirportData.MADAGASCAR, AirportData.SINGAPORE, priceForEachRoute);
        final Route singaporeAustralia = dataTestUtil
                .save(AirportData.SINGAPORE, AirportData.AUSTRALIA, priceForEachRoute);

        final List<Route> routes = routeDiscoveryService.cheapest(
                berlinBelgrade.getSource().getCity().getId(),
                singaporeAustralia.getDestination().getCity().getId());
        Assertions.assertEquals(5, routes.size());
        Assertions.assertEquals(berlinBelgrade, routes.get(0));
        Assertions.assertEquals(belgradeMoscow, routes.get(1));
        Assertions.assertEquals(moscowMadagascar, routes.get(2));
        Assertions.assertEquals(madagascarSingapore, routes.get(3));
        Assertions.assertEquals(singaporeAustralia, routes.get(4));
    }

    @Test
    void multipleDestinations() {
        final List<Route> routes = routeDiscoveryService.cheapest(mostar.getId(), berlin.getId());
        Assertions.assertEquals(2, routes.size());
        assertAirportNames(
                new String[]{
                        "Mostar International Airport",
                        "Il Caravaggio International Airport"
                },
                new String[]{
                        "Il Caravaggio International Airport",
                        "Berlin-Schönefeld International Airport"
                }, routes);
        assertPrices(new double[]{73.78, 44.45}, routes);
    }

    private void assertPrices(double[] expected, List<Route> actual) {
        Assertions.assertArrayEquals(expected,
                actual.stream().mapToDouble(Route::getPrice).toArray());
    }

    private void assertAirportNames(String[] expectedSources, String[] expectedDestinations, List<Route> actual) {
        Assertions.assertArrayEquals(expectedSources,
                actual.stream().map(r -> r.getSource().getName()).toArray());
        Assertions.assertArrayEquals(expectedDestinations,
                actual.stream().map(r -> r.getDestination().getName()).toArray());
    }

    @Test
    void farAway() {
        final List<Route> routes = routeDiscoveryService.cheapest(newZealand.getId(), mostar.getId());
        Assertions.assertEquals(4, routes.size());
        assertAirportNames(new String[]{
                "Kaikoura Airport",
                "John F Kennedy International Airport",
                "Dublin Airport",
                "Il Caravaggio International Airport",
        }, new String[]{
                "John F Kennedy International Airport",
                "Dublin Airport",
                "Il Caravaggio International Airport",
                "Mostar International Airport",
        }, routes);
        assertPrices(new double[]{220.01, 32.47, 23.71, 60.64}, routes);
    }

    @Test
    void bigHubs() {
        final List<Route> routes = routeDiscoveryService.cheapest(newYork.getId(), berlin.getId());
        Assertions.assertEquals(2, routes.size());
        assertAirportNames(new String[]{
                "John F Kennedy International Airport",
                "Miami International Airport",
        }, new String[]{
                "Miami International Airport",
                "Berlin-Tegel International Airport",
        }, routes);
        assertPrices(new double[]{22.11, 21.35}, routes);
    }

}
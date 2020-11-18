package com.example.airmin;

import com.example.airmin.model.City;
import com.example.airmin.model.Role;
import com.example.airmin.model.User;
import com.example.airmin.repository.CityRepository;
import com.example.airmin.repository.UserRepository;
import com.example.airmin.service.UserService;
import com.example.airmin.service.csv.AirportCsvColumns;
import com.example.airmin.service.csv.AirportCsvImporter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@Profile("!test")
public class AppRunner implements CommandLineRunner {

    private final UserService userService;
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final AirportCsvImporter airportCsvImporter;
    private final CSVFormat csvFormat;

    public AppRunner(final UserService userService, final UserRepository userRepository, final CityRepository cityRepository,
                     final AirportCsvImporter airportCsvImporter, final CSVFormat csvFormat) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.cityRepository = cityRepository;
        this.airportCsvImporter = airportCsvImporter;
        this.csvFormat = csvFormat;
    }

    @Override
    public void run(final String... args) throws Exception {
        if (userRepository.count() == 0 && has("initUsers", args)) {
            createUser("admin");
            createUser("user");
        }

        if (cityRepository.count() == 0 && has("initCities", args)) {
            final File airportsFile = ResourceUtils.getFile("classpath:datasets/airports.csv");
            final byte[] airports = Files.readAllBytes(airportsFile.toPath());

            Iterable<CSVRecord> records =
                    csvFormat.parse(new InputStreamReader(new ByteArrayInputStream(airports)));
            final Set<City> cities = StreamSupport.stream(records.spliterator(), false).map(record -> {
                final String city = airportCsvImporter.get(AirportCsvColumns.CITY, record);
                final String country = airportCsvImporter.get(AirportCsvColumns.COUNTRY, record);
                return new City(city, country, "lovely");
            }).collect(Collectors.toSet());
            cityRepository.saveAll(cities);
        }
    }

    private boolean has(final String arg, final String[] args) {
        return args.length > 0 && Arrays.stream(args).anyMatch(a -> a.contains(arg));
    }

    private void createUser(String id) {
        var admin = new User();
        admin.setUsername(id);
        admin.setPassword(id);
        admin.setFirstName(id);
        admin.setLastName(id);
        admin.setRoles(Collections.singleton(Role.ROLE_ADMIN));

        userService.register(admin);
    }

}

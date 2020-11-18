package com.example.airmin.rest;

import com.example.airmin.service.csv.AirportCsvImporter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class AirportController {

    private final AirportCsvImporter airportCsvImporter;

    public AirportController(final AirportCsvImporter airportCsvImporter) {
        this.airportCsvImporter = airportCsvImporter;
    }

    /**
     * Import airports from provided file
     *
     * @param file to import
     * @throws IOException when file read goes wrong
     */
    @PostMapping("/airports")
    @ResponseStatus(HttpStatus.OK)
    public void importData(@RequestParam("file") MultipartFile file) throws IOException {
        this.airportCsvImporter.importData(file.getBytes());
    }
}

package com.example.airmin.rest;

import com.example.airmin.model.Route;
import com.example.airmin.rest.dto.RouteDto;
import com.example.airmin.service.RouteDiscoveryService;
import com.example.airmin.service.csv.RouteCsvImporter;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class RouteController {

    private final RouteCsvImporter routeCsvImporter;
    private final RouteDiscoveryService routeDiscoveryService;
    private final ModelMapper modelMapper;

    public RouteController(final RouteCsvImporter routeCsvImporter, final RouteDiscoveryService routeDiscoveryService, final ModelMapper modelMapper) {
        this.routeCsvImporter = routeCsvImporter;
        this.routeDiscoveryService = routeDiscoveryService;
        this.modelMapper = modelMapper;
    }

    /**
     * Import routes from provided file
     *
     * @param file to import
     * @throws IOException when file read goes wrong
     */
    @PostMapping("/routes")
    @ResponseStatus(HttpStatus.OK)
    public void importData(@RequestParam("file") MultipartFile file) throws IOException {
        routeCsvImporter.importData(file.getBytes());
    }

    /**
     * Find cheapest route from city A to B
     *
     * @param sourceCityId      A
     * @param destinationCityId B
     * @return {@link ResponseEntity<RouteDto.WithTotal>} with {@link HttpStatus#OK} when route is found.
     * Otherwise {@link HttpStatus#NO_CONTENT}
     */
    @GetMapping("/routes/cheapest")
    public ResponseEntity<RouteDto.WithTotal> findCheapestRoute(
            @RequestParam(value = "source") Long sourceCityId,
            @RequestParam(value = "destination") Long destinationCityId
    ) {
        final List<Route> routes = routeDiscoveryService.cheapest(sourceCityId, destinationCityId);
        if (CollectionUtils.isEmpty(routes)) {
            return ResponseEntity.noContent().build();
        }

        final List<RouteDto> routesDto = routes.stream().map(r -> modelMapper.map(r, RouteDto.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new RouteDto.WithTotal(routes.stream().mapToDouble(Route::getPrice).sum(), routesDto));
    }
}

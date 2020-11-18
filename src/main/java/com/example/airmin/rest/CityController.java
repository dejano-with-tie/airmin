package com.example.airmin.rest;

import com.example.airmin.model.City;
import com.example.airmin.model.City_;
import com.example.airmin.rest.dto.CityDto;
import com.example.airmin.rest.dto.PageableDto;
import com.example.airmin.service.CityService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.stream.Collectors;

/**
 * Defined endpoints for {@link City} resource
 */
@RestController
@RequestMapping("/cities")
public class CityController {

    private final ModelMapper modelMapper;
    private final CityService cityService;

    public CityController(final ModelMapper modelMapper, final CityService cityService) {
        this.modelMapper = modelMapper;
        this.cityService = cityService;
    }

    /**
     * Create new city.
     * <p>
     * City is created if (city name, country name) pair doesn't already exist in database.
     *
     * @param city resource
     * @return {@link ResponseEntity} with status {@code 201 (created)} with the body of created city.
     */
    @PostMapping
    public ResponseEntity<CityDto> create(@RequestBody @Valid CityDto city) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(modelMapper.map(cityService.save(modelMapper.map(city, City.class)), CityDto.class));
    }

    /**
     * Get cities with latest comments
     *
     * @param nameQuery        query by name
     * @param numberOfComments maximum number of comments included with city. If not provided, all comments will be
     *                         returned
     * @param page             page
     * @param perPage          results per page
     * @return {@link ResponseEntity<PageableDto>} with status 200 with body of cities with latest comments
     */
    @GetMapping
    public ResponseEntity<PageableDto<CityDto>> getAll(@RequestParam(value = "name", required = false) String nameQuery,
                                                       @RequestParam(value = "comments-length", required = false) Long numberOfComments,
                                                       @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                                       @RequestParam(value = "limit", required = false, defaultValue = "10") Integer perPage) {
        final PageRequest paging = PageRequest.of(page, perPage, Sort.by(City_.id.getName()));

        final Page<City> paged = cityService.queryAll(nameQuery, numberOfComments, paging);

        return ResponseEntity.ok(new PageableDto<>(
                new PageableDto.Meta(paged.getTotalElements(), perPage, page),
                paged.getContent().stream().map(c -> modelMapper.map(c, CityDto.class)).collect(Collectors.toList())
        ));
    }

}

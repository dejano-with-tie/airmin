package com.example.airmin.rest;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.example.airmin.model.Airport_;
import com.example.airmin.model.City;
import com.example.airmin.model.Route;
import com.example.airmin.repository.AirportRepository;
import com.example.airmin.util.DataTestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_USER")
@SpringBootTest()
@ActiveProfiles("test")
class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AirportRepository airportRepository;
    @Autowired
    private DataTestUtil dataTestUtil;

    @AfterEach
    void tearDown() {
        dataTestUtil.deleteAll();
    }

    @Test
    void shortestRoute() throws Exception {
        dataTestUtil.save(DataTestUtil.AirportData.BELGRADE_TESLA, DataTestUtil.AirportData.ROME_LEONARDO, 200.21);
        dataTestUtil.save(DataTestUtil.AirportData.BELGRADE_TESLA, DataTestUtil.AirportData.PARIS, 70.1);
        dataTestUtil.save(DataTestUtil.AirportData.PARIS, DataTestUtil.AirportData.BERLIN_TEGEL, 10.1);
        dataTestUtil.save(DataTestUtil.AirportData.PARIS, DataTestUtil.AirportData.MOSCOW_SHEREMETYEVO, 100.32);
        dataTestUtil.save(DataTestUtil.AirportData.BERLIN_TEGEL, DataTestUtil.AirportData.MOSCOW_SHEREMETYEVO, 113.31);
        // cheapest route
        Route belgradeBerlin = dataTestUtil
                .save(DataTestUtil.AirportData.BELGRADE_TESLA, DataTestUtil.AirportData.BERLIN_TEGEL, 75.22);
        Route berlinRome = dataTestUtil
                .save(DataTestUtil.AirportData.BERLIN_TEGEL, DataTestUtil.AirportData.ROME_LEONARDO, 20.13);
        Route romeMoscow = dataTestUtil
                .save(DataTestUtil.AirportData.ROME_LEONARDO, DataTestUtil.AirportData.MOSCOW_SHEREMETYEVO, 50.02);

        final City belgrade = dataTestUtil.saveIfNotExist(DataTestUtil.CityData.BELGRADE);
        final City moscow = dataTestUtil.saveIfNotExist(DataTestUtil.CityData.MOSCOW);

        List<Route> routes = Stream.of(belgradeBerlin, berlinRome, romeMoscow).collect(Collectors.toList());
        routes.forEach(r -> {
            r.setDestination(airportRepository.findById(r.getDestination().getId(),
                    EntityGraphUtils.fromAttributePaths(Airport_.city.getName())).orElseThrow());
            r.setSource(airportRepository.findById(r.getSource().getId(),
                    EntityGraphUtils.fromAttributePaths(Airport_.city.getName())).orElseThrow());
        });

        final ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get(String.format("/routes/cheapest?source=%s&destination=%s", belgrade.getId(), moscow.getId()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.routes.length()").value(3))
                .andExpect(jsonPath("$.totalPrice")
                        .value(routes.stream().mapToDouble(Route::getPrice).sum()));

        for (int i = 0; i < routes.size(); i++) {
            var route = routes.get(i);
            // @formatter:off
            resultActions.andExpect(jsonPath("$.routes[" + i + "].price").value(route.getPrice()))
                    .andExpect(jsonPath("$.routes[" + i + "].departure.airportName").value(route.getSource().getName()))
                    .andExpect(jsonPath("$.routes[" + i + "].departure.city.id").value(route.getSource().getCity().getId()))
                    .andExpect(jsonPath("$.routes[" + i + "].departure.city.name").value(route.getSource().getCity().getName()))
                    .andExpect(jsonPath("$.routes[" + i + "].departure.city.country").value(route.getSource().getCity().getCountry()))
                    .andExpect(jsonPath("$.routes[" + i + "].arrival.airportName").value(route.getDestination().getName()))
                    .andExpect(jsonPath("$.routes[" + i + "].arrival.city.id").value(route.getDestination().getCity().getId()))
                    .andExpect(jsonPath("$.routes[" + i + "].arrival.city.name").value(route.getDestination().getCity().getName()))
                    .andExpect(jsonPath("$.routes[" + i + "].arrival.city.country").value(route.getDestination().getCity().getCountry()));
            // @formatter:on
        }
    }

    @Test
    void failCityNotFound() throws Exception {
        final City belgrade = dataTestUtil.save(DataTestUtil.CityData.BELGRADE);

        final String unknownCityId = "99999999";
        mockMvc.perform(MockMvcRequestBuilders
                .get(String.format("/routes/cheapest?source=%s&destination=%s", unknownCityId, belgrade.getId()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.code").value("RELATION_NOT_FOUND"));
        mockMvc.perform(MockMvcRequestBuilders
                .get(String.format("/routes/cheapest?source=%s&destination=%s", belgrade.getId(), unknownCityId))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.code").value("RELATION_NOT_FOUND"));

    }

    @Test
    void noRouteFound() throws Exception {
        final City belgrade = dataTestUtil.save(DataTestUtil.CityData.BELGRADE);
        final City berlin = dataTestUtil.save(DataTestUtil.CityData.BERLIN);

        mockMvc.perform(MockMvcRequestBuilders
                .get(String.format("/routes/cheapest?source=%s&destination=%s", berlin.getId(), belgrade.getId()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }
}
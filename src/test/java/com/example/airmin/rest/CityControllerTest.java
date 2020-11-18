package com.example.airmin.rest;

import com.example.airmin.model.City;
import com.example.airmin.model.Comment;
import com.example.airmin.model.User;
import com.example.airmin.repository.CityCommentRepository;
import com.example.airmin.repository.CityRepository;
import com.example.airmin.repository.UserRepository;
import com.example.airmin.rest.dto.CityDto;
import com.example.airmin.util.DataTestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the {@link CityController}.
 */
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_ADMIN")
@SpringBootTest()
@ActiveProfiles("test")
class CityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CityCommentRepository cityCommentRepository;

    @Autowired
    private DataTestUtil dataTestUtil;


    @AfterEach
    void tearDown() {
        cityCommentRepository.deleteAll();
        userRepository.deleteAll();
        cityRepository.deleteAllInBatch();
    }

    /**
     * Should successfully create city
     *
     * @throws Exception if mvc call goes wrong
     */
    @Test
    void createSuccessfully() throws Exception {
        final CityDto belgrade = dataTestUtil.createDto(DataTestUtil.CityData.BELGRADE);

        mockMvc.perform(MockMvcRequestBuilders.post("/cities").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(belgrade)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value(DataTestUtil.CityData.BELGRADE.getName()))
                .andExpect(jsonPath("$.country").value(DataTestUtil.CityData.BELGRADE.getCountry()))
                .andExpect(jsonPath("$.description").value(DataTestUtil.CityData.BELGRADE.getDescription()));

        final List<City> cities = cityRepository.findAll();
        Assertions.assertEquals(1, cities.size());
        Assertions.assertNotNull(cities.get(0));
        Assertions.assertEquals(belgrade.getName(), cities.get(0).getName());
        Assertions.assertEquals(belgrade.getCountry(), cities.get(0).getCountry());
        Assertions.assertEquals(belgrade.getDescription(), cities.get(0).getDescription());
    }

    /**
     * When provided city (name, country) already exist, should fail with status
     * {@link org.springframework.http.HttpStatus#UNPROCESSABLE_ENTITY}
     *
     * @throws Exception if mvc call goes wrong
     */
    @Test
    void failCreateWhenCityExist() throws Exception {
        dataTestUtil.save(DataTestUtil.CityData.BELGRADE);

        final CityDto toCreate = dataTestUtil.createDto(DataTestUtil.CityData.BELGRADE);

        mockMvc.perform(MockMvcRequestBuilders.post("/cities").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(toCreate)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("NOT_UNIQUE"))
                .andExpect(jsonPath("$.message").isNotEmpty());

        Assertions.assertEquals(1, cityRepository.count());
    }

    /**
     * Should fail when {@link CityDto} data don't satisfy bean validation
     *
     * @throws Exception if mvc call goes wrong
     */
    @Test
    void failCreateWhenInvalidProperties() throws Exception {
        final CityDto toCreate = new CityDto(DataTestUtil.CityData.BELGRADE.getName(), " ", "");

        mockMvc.perform(MockMvcRequestBuilders.post("/cities").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(toCreate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CONSTRAINT_VALIDATION"))
                .andExpect(jsonPath("$..errors[0].field").value("country"))
                .andExpect(jsonPath("$..errors[0].code").value("NotBlank"))
                .andExpect(jsonPath("$..errors[0].message").isNotEmpty())
                .andExpect(jsonPath("$..errors[0].rejectedValue").value(" "))
                .andExpect(jsonPath("$..errors[1].field").value("description"))
                .andExpect(jsonPath("$..errors[1].code").value("NotBlank"))
                .andExpect(jsonPath("$..errors[1].message").isNotEmpty())
                .andExpect(jsonPath("$..errors[1].rejectedValue").value(""))
        ;

        Assertions.assertEquals(0, cityRepository.count());
    }

    /**
     * Only admin roles are allowed to add new {@link City}
     *
     * @throws Exception if mvc call goes wrong
     */
    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void failCreateWhenNonAdmin() throws Exception {
        final CityDto toCreate = dataTestUtil.createDto(DataTestUtil.CityData.BELGRADE);

        mockMvc.perform(MockMvcRequestBuilders.post("/cities").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(toCreate)))
                .andExpect(status().isForbidden())
                .andExpect(content().string(""))
        ;

        Assertions.assertEquals(0, cityRepository.count());
    }

    /**
     * Should return all comments of a city when limit is not provided
     *
     * @throws Exception if mvc call goes wrong
     */
    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void getAllCitiesWithAllComments() throws Exception {
        prepareCitiesWithComments();

        mockMvc.perform(MockMvcRequestBuilders.get("/cities")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..meta.length").value(3))
                .andExpect(jsonPath("$..meta.limit").value(10))
                .andExpect(jsonPath("$..meta.page").value(0))
                .andExpect(jsonPath("$.results[0].name").value(DataTestUtil.CityData.BELGRADE.getName()))
                .andExpect(jsonPath("$.results[0]..comments.length()").value(3))
                .andExpect(jsonPath("$.results[0]..comments[0].content").value("Lovely city"))
                .andExpect(jsonPath("$.results[1]..comments.length()").value(1))
        ;
    }

    private void prepareCitiesWithComments() throws InterruptedException {
        var belgrade = dataTestUtil.save(DataTestUtil.CityData.BELGRADE);
        var berlin = dataTestUtil.save(DataTestUtil.CityData.BERLIN);
        dataTestUtil.save(DataTestUtil.CityData.STERLING);

        var john = new User("John", RandomString.make(), "John", "Doe");
        var george = new User("George", RandomString.make(), "George", "Doe");
        var marge = new User("Marge", RandomString.make(), "Marge", "Simpson");
        userRepository.saveAll(Arrays.asList(john, george, marge));

        // sleep to have gaps in createdAt timestamp
        cityCommentRepository.save(new Comment("Love this city", john, belgrade));
        cityCommentRepository.save(new Comment("Lot of scammers", george, belgrade));
        cityCommentRepository.save(new Comment("Lovely city", marge, belgrade));
        cityCommentRepository.save(new Comment("Awww Berlin", john, berlin));
    }

    /**
     * Should return all cities with at most {@code comments-length=2} comments sorted by
     * {@link Comment#getCreatedAt()}
     *
     * @throws Exception if mvc call goes wrong
     */
    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void commentsAreLimitedAndOrderedByDate() throws Exception {
        prepareCitiesWithComments();

        mockMvc.perform(MockMvcRequestBuilders.get("/cities?comments-length=2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..meta.length").value(3))
                .andExpect(jsonPath("$..meta.limit").value(10))
                .andExpect(jsonPath("$..meta.page").value(0))
                .andExpect(jsonPath("$.results[0].name").value(DataTestUtil.CityData.BELGRADE.getName()))
                .andExpect(jsonPath("$.results[0]..comments.length()").value(2))
                .andExpect(jsonPath("$.results[0]..comments[0].content").value("Lovely city"))
        ;

        mockMvc.perform(MockMvcRequestBuilders.get("/cities")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..meta.length").value(3))
                .andExpect(jsonPath("$..meta.limit").value(10))
                .andExpect(jsonPath("$..meta.page").value(0))
                .andExpect(jsonPath("$.results[0].name").value(DataTestUtil.CityData.BELGRADE.getName()))
                .andExpect(jsonPath("$.results[0]..comments.length()").value(3))
                .andExpect(jsonPath("$.results[0]..comments[2].content").value("Love this city"))
                .andExpect(jsonPath("$.results[0]..comments[2].author").value("John"))
                .andExpect(jsonPath("$.results[1]..comments[0].content").value("Awww Berlin"))
                .andExpect(jsonPath("$.results[1]..comments[0].author").value("John"));
    }

    /**
     * Should return only cities that match the provided query
     *
     * @throws Exception if mvc call goes wrong
     */
    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void onlyCitiesWithMatchingName() throws Exception {
        dataTestUtil.save(DataTestUtil.CityData.BELGRADE);
        dataTestUtil.save(DataTestUtil.CityData.BERLIN);
        dataTestUtil.save(DataTestUtil.CityData.STERLING);

        mockMvc.perform(MockMvcRequestBuilders.get("/cities?name=erli")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..meta.length").value(2))
                .andExpect(jsonPath("$..meta.limit").value(10))
                .andExpect(jsonPath("$..meta.page").value(0))
                .andExpect(jsonPath("$.results.length()").value(2))
                .andExpect(jsonPath("$.results[0].name").value(DataTestUtil.CityData.BERLIN.getName()))
                .andExpect(jsonPath("$.results[1].name").value(DataTestUtil.CityData.STERLING.getName()));
    }

    /**
     * Empty results when there are cities which match the query
     *
     * @throws Exception if mvc call goes wrong
     */
    @Test
    void searchNoResults() throws Exception {
        dataTestUtil.save(DataTestUtil.CityData.BELGRADE);
        mockMvc.perform(MockMvcRequestBuilders.get("/cities?name=erli")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..meta.length").value(0))
                .andExpect(jsonPath("$..meta.limit").value(10))
                .andExpect(jsonPath("$..meta.page").value(0))
                .andExpect(jsonPath("$.results.length()").value(0));
    }

}
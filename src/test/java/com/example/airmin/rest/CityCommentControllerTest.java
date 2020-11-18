package com.example.airmin.rest;

import com.example.airmin.model.City;
import com.example.airmin.model.Comment;
import com.example.airmin.model.User;
import com.example.airmin.repository.CityCommentRepository;
import com.example.airmin.repository.CityRepository;
import com.example.airmin.repository.UserRepository;
import com.example.airmin.rest.dto.CityCommentDto;
import com.example.airmin.util.DataTestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the {@link CityCommentController}.
 */
@AutoConfigureMockMvc
@WithMockUser(authorities = "ROLE_USER")
@SpringBootTest()
@ActiveProfiles("test")
class CityCommentControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private CityCommentRepository cityCommentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DataTestUtil dataTestUtil;

    private City belgrade;
    private User authenticatedUser;

    @BeforeEach
    void setUp() {
        belgrade = dataTestUtil.save(DataTestUtil.CityData.BELGRADE);
        authenticatedUser = userRepository.save(new User("user", RandomString.make(), "last name", "last name"));
    }

    @AfterEach
    void tearDown() {
        cityCommentRepository.deleteAll();
        cityRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * Should successfully add a comment to a city
     *
     * @throws Exception mvc
     */
    @Test
    void commentSuccessfully() throws Exception {
        final String content = "hello friend";
        final var toCreate = new CityCommentDto.Create(content);

        mockMvc.perform(MockMvcRequestBuilders.post(String.format("/cities/%s/comments", belgrade.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(toCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.content").value(content))
                .andExpect(jsonPath("$.author").value(authenticatedUser.getUsername()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.modifiedAt").isNotEmpty());

        Assertions.assertEquals(1, cityCommentRepository.count());
    }

    /**
     * Commenting on non-existing city should result in 404 response
     *
     * @throws Exception mvc
     */
    @Test
    void failCommentingWhenCityNotFound() throws Exception {
        final String content = "hello friend";
        final var toCreate = new CityCommentDto.Create(content);

        final String unknownCityId = "44";
        mockMvc.perform(MockMvcRequestBuilders
                .post(String.format("/cities/%s/comments", unknownCityId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(toCreate)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.code").value("RELATION_NOT_FOUND"));

        Assertions.assertEquals(0, cityCommentRepository.count());
    }

    /**
     * Should fail when provided comment content does not satisfy validation (non null etc)
     *
     * @throws Exception mvc
     */
    @Test
    void failCommentingWhenInvalidContent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .post(String.format("/cities/%s/comments", belgrade.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new CityCommentDto.Create(" "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.code").value("CONSTRAINT_VALIDATION"));

        mockMvc.perform(MockMvcRequestBuilders
                .post(String.format("/cities/%s/comments", belgrade.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new CityCommentDto.Create(RandomString.make(226)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.code").value("CONSTRAINT_VALIDATION"));

        Assertions.assertEquals(0, cityCommentRepository.count());
    }

    @Test
    void deleteSuccessfully() throws Exception {
        final Comment comment = new Comment("hello", authenticatedUser, belgrade);
        cityCommentRepository.save(comment);

        mockMvc.perform(MockMvcRequestBuilders
                .delete(String.format("/cities/%s/comments/%s", belgrade.getId(), comment.getId()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        Assertions.assertEquals(0, cityCommentRepository.count());
    }

    @Test
    void failDeleteWhenNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .delete(String.format("/cities/%s/comments/%s", belgrade.getId(), 1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));

        Assertions.assertEquals(0, cityCommentRepository.count());
    }

    @Test
    void failDeleteWhenDifferentAuthor() throws Exception {
        final User randomUser = userRepository
                .save(new User(RandomString.make(), RandomString.make(), "last name", "last name"));
        final Comment comment = cityCommentRepository.save(new Comment("hello", randomUser, belgrade));

        mockMvc.perform(MockMvcRequestBuilders
                .delete(String.format("/cities/%s/comments/%s", belgrade.getId(), comment.getId()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));

        Assertions.assertEquals(1, cityCommentRepository.count());
    }

    @Test
    void updateSuccessfully() throws Exception {
        final Comment comment = new Comment("hello", authenticatedUser, belgrade);
        cityCommentRepository.save(comment);

        mockMvc.perform(MockMvcRequestBuilders
                .put(String.format("/cities/%s/comments/%s", belgrade.getId(), comment.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new CityCommentDto.Create("updated"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(comment.getId()))
                .andExpect(jsonPath("$.content").value("updated"))
                .andExpect(jsonPath("$.author").value(authenticatedUser.getUsername()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.modifiedAt").isNotEmpty());

        Assertions.assertEquals(1, cityCommentRepository.count());
        Assertions.assertTrue(cityCommentRepository.findAll().get(0).getModifiedAt().isAfter(comment.getModifiedAt()));
    }

    @Test
    void failUpdateDifferentAuthor() throws Exception {
        final User randomUser = userRepository
                .save(new User(RandomString.make(), RandomString.make(), "last name", "last name"));
        final Comment comment = cityCommentRepository.save(new Comment("hello", randomUser, belgrade));

        mockMvc.perform(MockMvcRequestBuilders
                .put(String.format("/cities/%s/comments/%s", belgrade.getId(), comment.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new CityCommentDto.Create("updated"))))
                .andExpect(status().isNotFound())
                .andExpect(content().string(""));

        final List<Comment> comments = cityCommentRepository.findAll();
        Assertions.assertEquals(1, comments.size());
        Assertions.assertEquals(comment, comments.get(0));
    }
}
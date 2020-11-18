package com.example.airmin.service;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.example.airmin.model.City;
import com.example.airmin.model.City_;
import com.example.airmin.model.Comment;
import com.example.airmin.model.Comment_;
import com.example.airmin.repository.CityCommentRepository;
import com.example.airmin.repository.CityRepository;
import com.example.airmin.rest.exception.CityAlreadyExistException;
import com.example.airmin.rest.exception.ResourceNotFoundException;
import com.example.airmin.rest.exception.common.ApiErrorCode;
import com.example.airmin.security.SecurityUtil;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CityService {

    private final CityRepository cityRepository;
    private final CityCommentRepository cityCommentRepository;
    private final SecurityUtil securityUtil;

    public CityService(final CityRepository cityRepository, final CityCommentRepository cityCommentRepository, final SecurityUtil securityUtil) {
        this.cityRepository = cityRepository;
        this.cityCommentRepository = cityCommentRepository;
        this.securityUtil = securityUtil;
    }

    /**
     * Persist provided {@link City} into database.
     *
     * @param city to save
     * @return Saved {@link City}
     * @throws CityAlreadyExistException when city with given lowercase name and lowercase country already exist.
     */
    @PreAuthorize("hasAuthority(T(com.example.airmin.model.Role).ROLE_ADMIN.name())")
    public City save(@NonNull City city) {
        final Optional<City> inDb = cityRepository
                .findByNameIgnoreCaseAndCountryIgnoreCase(city.getName(), city.getCountry());
        if (inDb.isPresent()) {
            throw new CityAlreadyExistException(inDb.get().getName(), inDb.get().getCountry());
        }

        city.setId(null);
        return cityRepository.save(city);
    }

    /**
     * Add {@link Comment} to {@link City}. Currently authenticated user is author of a comment.
     *
     * @param comment comment
     * @param cityId  city to comment on
     * @return saved {@link Comment}
     * @throws ResourceNotFoundException when provided {@code cityId} doesn't exist.
     */
    public Comment saveComment(final Comment comment, final Long cityId) {
        final City city = cityRepository.findById(cityId).orElseThrow(() ->
                new ResourceNotFoundException(String
                        .format("City with id '%s' doesn't exist", cityId), ApiErrorCode.RELATION_NOT_FOUND));

        comment.setId(null);
        comment.setCity(city);
        comment.setAuthor(securityUtil.getUser());
        return cityCommentRepository.save(comment);
    }

    /**
     * Delete a {@link Comment}
     *
     * @param commentId to delete
     * @throws ResourceNotFoundException when currently authenticated user is not an author of a comment
     */
    public void delete(final Long commentId) {
        var toDelete =
                cityCommentRepository.findByIdAndAuthorId(commentId, securityUtil.getUser().getId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                String.format("Comment with id '%s' does not exist", commentId), ApiErrorCode.NOT_FOUND));
        cityCommentRepository.delete(toDelete);
    }

    /**
     * Updates content of a {@link Comment}
     *
     * @param commentId to update
     * @param content   new comment's content
     * @return updated {@link Comment}
     * @throws ResourceNotFoundException when currently authenticated user is not an author of a comment
     */
    public Comment update(final Long commentId, final String content) {
        var toUpdate =
                cityCommentRepository.findByIdAndAuthorId(commentId, securityUtil.getUser().getId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                String.format("Comment with id '%s' does not exist", commentId), ApiErrorCode.NOT_FOUND));

        toUpdate.setContent(content);
        return cityCommentRepository.save(toUpdate);
    }

    /**
     * Search {@link City} by name.
     * <p>
     * When {@code nameQuery} is not provided, all cities are returned limited by given {@code paging}.
     * <p>
     * Otherwise,
     * querying is performed by checking if {@link City#getName()} contains provided {@code nameQuery}
     * <p>
     *
     * @param nameQuery        name query
     * @param numberOfComments maximum number of latest comments contained in a {@link City}
     * @param paging           paging
     * @return Cities with latest comments
     */
    public Page<City> queryAll(final String nameQuery, final Long numberOfComments, final PageRequest paging) {
        Page<City> cities = searchCities(nameQuery, paging);
        cities.getContent().forEach(city -> city.setComments(sortAndLimit(city.getComments(), numberOfComments)));
        return cities;
    }

    private List<Comment> sortAndLimit(final Collection<Comment> comments, final Long limit) {
        Stream<Comment> sorted = comments
                .stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt).reversed());
        if (limit != null) {
            sorted = sorted.limit(limit);
        }
        return sorted.collect(Collectors.toList());
    }

    /**
     * Find cities with all comments fetched, queried by {@code nameQuery} contained in {@link City#getName()}
     * <p>
     * Because Hibernate is not able to efficiently limit and join in one query, search is accomplished by 2 (+2 count)
     * queries.
     *
     * @param nameQuery name query
     * @param paging    paging
     * @return Cities with all latest comments
     */
    private Page<City> searchCities(@Nullable final String nameQuery, final PageRequest paging) {
        final Page<City> cities = StringUtils.hasText(nameQuery)
                ? cityRepository.findByNameIsContainingIgnoreCaseOrderById(nameQuery, paging)
                : cityRepository.findAll(paging);

        final EntityGraph relations = EntityGraphUtils.fromAttributePaths(
                City_.comments.getName(),
                City_.comments.getName().concat(".").concat(Comment_.author.getName()));
        final List<City> withRelations = cityRepository.findDistinctByIdIn(
                cities.stream().map(City::getId).collect(Collectors.toList()),
                paging.getSort(), relations);
        return new PageImpl<>(withRelations, paging, cities.getTotalElements());
    }
}

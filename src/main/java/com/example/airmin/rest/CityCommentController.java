package com.example.airmin.rest;

import com.example.airmin.model.City;
import com.example.airmin.model.Comment;
import com.example.airmin.rest.dto.CityCommentDto;
import com.example.airmin.service.CityService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/cities/{cityId}")
public class CityCommentController {

    private final ModelMapper modelMapper;
    private final CityService cityService;

    public CityCommentController(final ModelMapper modelMapper, final CityService cityService) {
        this.modelMapper = modelMapper;
        this.cityService = cityService;
    }

    /**
     * Add a {@link Comment} to a {@link City}
     *
     * @param commentDto comment
     * @param cityId     to comment on
     * @return {@link ResponseEntity<CityCommentDto>} with status {@link HttpStatus#CREATED}
     */
    @PostMapping("/comments")
    public ResponseEntity<CityCommentDto> create(@Valid @RequestBody CityCommentDto.Create commentDto,
                                                 @PathVariable Long cityId) {
        final Comment saved = cityService.saveComment(modelMapper.map(commentDto, Comment.class), cityId);
        return ResponseEntity.status(HttpStatus.CREATED).body(modelMapper.map(saved, CityCommentDto.class));
    }

    /**
     * Delete a comment. Response status on success is {@link HttpStatus#NO_CONTENT}. If provided comment does not
     * belong to currently authenticated user, status is {@link HttpStatus#NOT_FOUND}
     *
     * @param commentId to delete
     */
    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long commentId) {
        cityService.delete(commentId);
    }

    /**
     * Update comment
     *
     * @param commentDto new content
     * @param commentId  to update
     * @return {@link ResponseEntity<CityCommentDto>} with status {@link HttpStatus#OK}
     */
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CityCommentDto> update(@Valid @RequestBody CityCommentDto.Create commentDto,
                                                 @PathVariable Long commentId) {
        final Comment updated = cityService.update(commentId, modelMapper.map(commentDto, Comment.class).getContent());
        return ResponseEntity.ok(modelMapper.map(updated, CityCommentDto.class));
    }

}

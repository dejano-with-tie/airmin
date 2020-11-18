package com.example.airmin.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
public class CityDto {

    private Long id;
    @NotBlank
    @Size(max = 120)
    private String name;
    @NotBlank()
    @Size(max = 120)
    private String country;
    @NotBlank
    @Size(max = 255)
    private String description;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<CityCommentDto> comments;

    public CityDto(@NotBlank final String name, @NotBlank final String country, @NotBlank final String description) {
        this.name = name;
        this.country = country;
        this.description = description;
    }
}

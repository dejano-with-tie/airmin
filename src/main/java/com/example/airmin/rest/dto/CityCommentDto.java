package com.example.airmin.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data

public class CityCommentDto {

    private Long id;
    private String content;
    private String author;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Create {
        @NotBlank
        @Size(max = 225)
        private String content;
    }
}

package com.example.airmin.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

/**
 * Wraps results with additional data useful for pagination.
 *
 * @param <T> type of resulting collection
 */
@Getter
@Setter
@AllArgsConstructor
public class PageableDto<T> {
    private Meta meta;
    private Collection<T> results;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Meta {
        private long length;
        private int limit;
        private int page;
    }

}

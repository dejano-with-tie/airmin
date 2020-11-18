package com.example.airmin.service.csv;

import lombok.Getter;

import java.util.function.Function;

/**
 * Routes csv file columns and parsers
 */
@Getter
public enum RouteCsvColumns implements CsvColumn {
    AIRLINE_CODE(0),
    AIRLINE_ID(1, Long::parseLong),
    SOURCE_AIRPORT(2),
    SOURCE_AIRPORT_ID(3, Long::parseLong),
    DESTINATION_AIRPORT(4),
    DESTINATION_AIRPORT_ID(5, Long::parseLong),
    CODE_SHARE(6, "Y"::equals),
    NUMBER_OF_STOPS(7, Integer::parseInt),
    EQUIPMENT(8),
    PRICE(9, Double::parseDouble),
    ;

    private final Function<String, Object> parser;
    private final int index;

    RouteCsvColumns(int index, final Function<String, Object> type) {
        this.parser = type;
        this.index = index;
    }

    RouteCsvColumns(int index) {
        this.parser = o -> o;
        this.index = index;
    }
}

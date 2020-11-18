package com.example.airmin.service.csv;

import java.util.function.Function;


/**
 * Column index and how to parse it from csv file
 */
public interface CsvColumn {
    Function<String, Object> getParser();
    int getIndex();
}

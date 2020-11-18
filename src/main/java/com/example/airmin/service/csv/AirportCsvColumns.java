package com.example.airmin.service.csv;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;

import org.jetbrains.annotations.Nullable;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.function.Function;

/**
 * Airports csv file columns and parsers
 */
@Getter
@Log4j2
public enum AirportCsvColumns implements CsvColumn {
    ID(0, Long::parseLong),
    NAME(1),
    CITY(2),
    COUNTRY(3),
    IATA_CODE(4),
    ICAO_CODE(5),
    LATITUDE(6, Double::parseDouble),
    LONGITUDE(7, Double::parseDouble),
    ALTITUDE(8, Integer::parseInt),
    TIMEZONE_OFFSET(9, AirportCsvColumns::parseTimezoneOffset),
    DAYLIGHT_SAVING(10),
    TIMEZONE(11, AirportCsvColumns::parseTimezoneId),
    TYPE(12),
    SOURCE(13);

    private final Function<String, Object> parser;
    private final int index;

    AirportCsvColumns(int index, final Function<String, Object> type) {
        this.parser = type;
        this.index = index;
    }

    AirportCsvColumns(int index) {
        this.parser = (o -> o);
        this.index = index;
    }

    @Nullable
    public static Object parseTimezoneId(final String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return ZoneId.of(value);
    }

    @Nullable
    public static ZoneOffset parseTimezoneOffset(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        try {
            value = StringUtils.trimAllWhitespace(value);
            char sign = value.charAt(0) == '-' ? '-' : '+';
            final double numberValue = Math.abs(Double.parseDouble(value));
            final int hours = (int) numberValue;
            double minutes = (numberValue - hours) * 60;
            String offset = String.format("%s%02d:%02d", sign, hours, (int)minutes);
            return ZoneOffset.of(offset);
        } catch (NumberFormatException | DateTimeException e) {
            log.trace(String.format("Unable to parse '%s' to ZoneOffset", value));
            log.trace(e.getMessage(), e);
        }

        return null;
    }
}
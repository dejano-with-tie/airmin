package com.example.airmin.service.csv;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class AirportCsvColumnsTest {

    @Test
    void parseTimeZoneOffset() {
        Assertions.assertEquals(ZoneOffset.of("+00:00"), AirportCsvColumns.parseTimezoneOffset("0"));
        Assertions.assertEquals(ZoneOffset.of("+00:00"), AirportCsvColumns.parseTimezoneOffset("-0"));
        Assertions.assertEquals(ZoneOffset.of("+01:00"), AirportCsvColumns.parseTimezoneOffset("1"));
        Assertions.assertEquals(ZoneOffset.of("-01:00"), AirportCsvColumns.parseTimezoneOffset("-1"));
        Assertions.assertEquals(ZoneOffset.of("+01:30"), AirportCsvColumns.parseTimezoneOffset("1.5"));
        Assertions.assertEquals(ZoneOffset.of("-02:30"), AirportCsvColumns.parseTimezoneOffset(" -2 .5 "));
    }
}
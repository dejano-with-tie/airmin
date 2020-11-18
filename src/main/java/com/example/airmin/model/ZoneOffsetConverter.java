package com.example.airmin.model;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.ZoneOffset;

/**
 * Converts {@link ZoneOffset} to a persistable type {@link String} and vice-versa
 */
@Converter
public class ZoneOffsetConverter implements AttributeConverter<ZoneOffset, String> {

    @Override
    public String convertToDatabaseColumn(ZoneOffset zoneOffset) {
        return zoneOffset == null ? null : zoneOffset.getId();
    }

    @Override
    public ZoneOffset convertToEntityAttribute(String zoneOffset) {
        return zoneOffset == null ? null : ZoneOffset.of(zoneOffset);
    }
}

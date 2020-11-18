package com.example.airmin.service.csv;

import com.example.airmin.model.Airport;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.access.prepost.PreAuthorize;

import org.jetbrains.annotations.Nullable;
import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

@Log4j2
public abstract class CsvImporter<T> {

    protected final CSVFormat csvFormat;
    protected final CacheManager cacheManager;

    public CsvImporter(final CSVFormat csvFormat, final CacheManager cacheManager) {
        this.csvFormat = csvFormat;
        this.cacheManager = cacheManager;
    }

    @Transactional
    @SneakyThrows(IOException.class)
    public void importData(final @NonNull File file) {
        importData(Files.readAllBytes(file.toPath()));
    }

    /**
     * This could be improved by scheduling a job and returning response immediately with the link to corresponding
     * created job resource. Or use spring batch
     *
     * @param fileContent to import
     */
    @Transactional
    @SneakyThrows(IOException.class)
    @PreAuthorize("hasAuthority(T(com.example.airmin.model.Role).ROLE_ADMIN.name())")
    public void importData(final @NonNull byte[] fileContent) {
        Objects.requireNonNull(cacheManager.getCache(Airport.class.getSimpleName().toLowerCase())).clear();
        persist(process(getCsvRecords(fileContent)));
    }

    public Iterable<CSVRecord> getCsvRecords(final byte[] fileContent) throws IOException {
        return csvFormat.parse(new InputStreamReader(new ByteArrayInputStream(fileContent)));
    }

    protected abstract List<T> process(Iterable<CSVRecord> records);

    @Transactional
    protected abstract void persist(List<T> items);

    @SuppressWarnings("unchecked")
    @Nullable
    public <R> R get(CsvColumn column, CSVRecord record) {
        try {
            return (R) column.getParser().apply(record.get(column.getIndex()));
        } catch (Exception e) {
            log.trace("Unable to parse record; {}", record);
            log.trace(e.getMessage(), e);
        }
        return null;
    }

}

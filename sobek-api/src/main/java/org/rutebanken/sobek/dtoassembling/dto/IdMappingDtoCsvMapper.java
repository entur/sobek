package org.rutebanken.sobek.dtoassembling.dto;

import com.google.common.base.Joiner;
import org.rutebanken.sobek.time.ExportTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class IdMappingDtoCsvMapper {


    private final ExportTimeZone exportTimeZone;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern( "yyyy-MM-dd'T'HH:mm:ss");
    private static final String SEPARATOR = ",";

    @Autowired
    public IdMappingDtoCsvMapper(ExportTimeZone exportTimeZone) {
        this.exportTimeZone = exportTimeZone;
    }

    public String toCsvString(IdMappingDto dto, boolean includeValidityInterval) {
        return toCsvString(dto, includeValidityInterval, true);
    }

    public String toCsvString(IdMappingDto dto, boolean includeValidityInterval, boolean includeNetexId) {

        List<String> stringList = new ArrayList<>();

        stringList.add(dto.originalId);

        if(includeNetexId) {
            stringList.add(dto.netexId);
        }

        if (includeValidityInterval) {
            stringList.add(toString(dto.validFrom));
            stringList.add(toString(dto.validTo));
        }

        return Joiner.on(SEPARATOR).join(stringList);
    }

    private String toString(Instant instant) {
        return instant == null ? "" : instant.atZone(exportTimeZone.getDefaultTimeZoneId()).format(DATE_TIME_FORMATTER);
    }

}

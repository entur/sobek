package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.mapstruct.Mapper;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.time.ExportTimeZone;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

/**
 * MapStruct mapper for temporal type conversions.
 * Provides bidirectional mapping between various Java time types and Instant.
 */
@Mapper
public abstract class TemporalTypeMapper {

    @Autowired
    protected ExportTimeZone exportTimeZone;

    // LocalDateTime <-> Instant
    public Instant localDateTimeToInstant(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(exportTimeZone.getDefaultTimeZoneId()).toInstant();
    }

    public LocalDateTime instantToLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(exportTimeZone.getDefaultTimeZoneId()).toLocalDateTime();
    }

    // ZonedDateTime <-> Instant
    public Instant zonedDateTimeToInstant(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }
        return Instant.from(zonedDateTime);
    }

    public ZonedDateTime instantToZonedDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(exportTimeZone.getDefaultTimeZoneId());
    }

    // OffsetDateTime <-> Instant
    public Instant offsetDateTimeToInstant(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return Instant.from(offsetDateTime);
    }

    public OffsetDateTime instantToOffsetDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(exportTimeZone.getDefaultTimeZoneId()).toOffsetDateTime();
    }
}
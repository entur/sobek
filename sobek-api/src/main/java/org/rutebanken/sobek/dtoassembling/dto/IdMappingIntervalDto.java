package org.rutebanken.sobek.dtoassembling.dto;

import java.time.Instant;

public class IdMappingIntervalDto {
    public String originalId;
    public String netexId;
    public Instant validFrom;
    public Instant validTo;

    private static final String SEPARATOR = ",";

    public IdMappingIntervalDto(String originalId, String netexId, Instant validFrom, Instant validTo) {
        this.originalId = originalId;
        this.netexId = netexId;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }
}

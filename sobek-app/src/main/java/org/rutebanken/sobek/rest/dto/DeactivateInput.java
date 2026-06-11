package org.rutebanken.sobek.rest.dto;

import java.time.Instant;

public record DeactivateInput (
    String netexId,
    Long version,
    Instant deactivateAt
) {}

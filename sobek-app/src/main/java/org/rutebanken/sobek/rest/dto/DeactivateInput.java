package org.rutebanken.sobek.rest.dto;

import java.time.Instant;

public record DeactivateInput (
    String NetexId,
    Long Version,
    Instant DeactivationDate
) {}

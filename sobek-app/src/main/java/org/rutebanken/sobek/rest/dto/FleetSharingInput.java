package org.rutebanken.sobek.rest.dto;

import java.util.List;

/**
 * The input of shareFleet and unshareFleet: a fleet, whether it is about everyone, and the organisations it is about.
 * Both are optional, but not both may be empty.
 */
public record FleetSharingInput(
    String fleetNetexId,
    Boolean everyone,
    List<String> organisationRefs
) {
    public boolean isEveryone() {
        return Boolean.TRUE.equals(everyone);
    }
}

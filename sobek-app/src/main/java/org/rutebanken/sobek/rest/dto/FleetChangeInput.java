package org.rutebanken.sobek.rest.dto;

import java.util.List;

/**
 * The input of addToFleet and removeFromFleet: a fleet, and references to the vehicles and the vehicle types to add or
 * remove. Both lists are optional, but not both may be empty.
 */
public record FleetChangeInput(
    String fleetNetexId,
    List<Reference> vehicles,
    List<Reference> transportTypes
) {
    public record Reference(String netexId) {}

    public List<String> vehicleNetexIds() {
        return netexIds(vehicles);
    }

    public List<String> transportTypeNetexIds() {
        return netexIds(transportTypes);
    }

    private static List<String> netexIds(List<Reference> references) {
        return references == null ? List.of() : references.stream().map(Reference::netexId).toList();
    }
}

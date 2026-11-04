package org.rutebanken.sobek.model.job;

import org.rutebanken.sobek.model.vehicle.VehicleTypeKindEnumeration;

/**
 * Which kinds of vehicle types an export should contain.
 */
public enum VehicleTypeKindSelection {
    PHYSICAL,
    CONTRACTUAL,
    BOTH;

    /**
     * @return the single kind to filter on, or null when both kinds are wanted
     */
    public VehicleTypeKindEnumeration toKindOrNull() {
        return switch (this) {
            case PHYSICAL -> VehicleTypeKindEnumeration.PHYSICAL;
            case CONTRACTUAL -> VehicleTypeKindEnumeration.CONTRACTUAL;
            case BOTH -> null;
        };
    }

    /**
     * Vehicles only ever refer to physical vehicle types, so they are only exported along with those.
     */
    public boolean includesPhysical() {
        return this != CONTRACTUAL;
    }
}

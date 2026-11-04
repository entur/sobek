package org.rutebanken.sobek.model.vehicle;

/**
 * Distinguishes the two usages of a VehicleType.
 * <ul>
 *   <li>PHYSICAL: a group of actual vehicles that exist in the real world. Vehicles may refer to it.</li>
 *   <li>CONTRACTUAL: a theoretical description of the vehicle type an operator and an authority have agreed on.
 *   No Vehicle may refer to it.</li>
 * </ul>
 * The kind is fixed when the VehicleType is first created.
 */
public enum VehicleTypeKindEnumeration {
    PHYSICAL("physical"),
    CONTRACTUAL("contractual");

    private final String value;

    VehicleTypeKindEnumeration(String v) {
        this.value = v;
    }

    public String value() {
        return this.value;
    }

    public static VehicleTypeKindEnumeration fromValue(String v) {
        for (VehicleTypeKindEnumeration c : values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        throw new IllegalArgumentException(v);
    }
}

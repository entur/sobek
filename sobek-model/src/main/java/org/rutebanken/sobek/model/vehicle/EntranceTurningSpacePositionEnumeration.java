package org.rutebanken.sobek.model.vehicle;

public enum EntranceTurningSpacePositionEnumeration {
    NONE("none"),
    OUTSIDE("outside"),
    INSIDE("inside"),
    INSIDE_AND_OUTSIDE("insideAndOutside");

    private final String value;

    private EntranceTurningSpacePositionEnumeration(String v) {
        this.value = v;
    }

    public String value() {
        return this.value;
    }

    public static EntranceTurningSpacePositionEnumeration fromValue(String v) {
        for(EntranceTurningSpacePositionEnumeration c : values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        throw new IllegalArgumentException(v);
    }
}

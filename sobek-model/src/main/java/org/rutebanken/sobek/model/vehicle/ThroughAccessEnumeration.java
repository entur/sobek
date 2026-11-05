package org.rutebanken.sobek.model.vehicle;

public enum ThroughAccessEnumeration {
    NO_THROUGH_ACCESS("noThroughAccess"),
    OPEN_ENTRANCE("openEntrance"),
    DOOR("door"),
    UNKNOWN("unknown");

    private final String value;

    ThroughAccessEnumeration(String v) {
        this.value = v;
    }

    public String value() {
        return this.value;
    }

    public static ThroughAccessEnumeration fromValue(String v) {
        for (ThroughAccessEnumeration c : values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        throw new IllegalArgumentException(v);
    }
}

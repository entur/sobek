package org.rutebanken.sobek.model.vehicle;

public enum NecessaryForceEnumeration {
    NO_FORCE("noForce"),
    LIGHT_FORCE("lightForce"),
    MEDIUM_FORCE("mediumForce"),
    HEAVY_FORCE("heavyForce"),
    UNKNOWN("unknown");

    private final String value;

    private NecessaryForceEnumeration(String v) {
        this.value = v;
    }

    public String value() {
        return this.value;
    }

    public static NecessaryForceEnumeration fromValue(String v) {
        for(NecessaryForceEnumeration c : values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        throw new IllegalArgumentException(v);
    }
}

package org.rutebanken.sobek.model.vehicle;

public enum CompassBearing8Enumeration {
    SW,
    SE,
    NW,
    NE,
    W,
    E,
    S,
    N;

    public String value() {
        return this.name();
    }

    public static CompassBearing8Enumeration fromValue(String v) {
        return valueOf(v);
    }
}

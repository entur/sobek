package org.rutebanken.sobek.model.vehicle;

public enum DirectionOfUseEnumeration {
    UP("up"),
    DOWN("down"),
    BOTH("both");

    private final String value;

    private DirectionOfUseEnumeration(String v) {
        this.value = v;
    }

    public String value() {
        return this.value;
    }

    public static DirectionOfUseEnumeration fromValue(String v) {
        for(DirectionOfUseEnumeration c : values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        throw new IllegalArgumentException(v);
    }
}

package org.rutebanken.sobek.model.vehicle;

public enum HandrailEnumeration {
    NONE("none"),
    ONE_SIDE("oneSide"),
    BOTH_SIDES("bothSides");

    private final String value;

    private HandrailEnumeration(String v) {
        this.value = v;
    }

    public String value() {
        return this.value;
    }

    public static HandrailEnumeration fromValue(String v) {
        for(HandrailEnumeration c : values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        throw new IllegalArgumentException(v);
    }
}

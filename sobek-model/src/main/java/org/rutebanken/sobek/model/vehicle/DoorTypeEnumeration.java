package org.rutebanken.sobek.model.vehicle;

public enum DoorTypeEnumeration {
    HINGED_SINGLE("hingedSingle"),
    HINGED_PAIR("hingedPair"),
    SLIDING_SINGLE("slidingSingle"),
    SLIDING_PAIR("slidingPair"),
    FOLDING_SINGLE("foldingSingle"),
    FOLDING_PAIR("foldingPair"),
    HINGED_RAMP("hingedRamp"),
    REVOLVING("revolving"),
    OTHER("other"),
    NONE("none"),
    UNKNOWN("unknown");

    private final String value;

    private DoorTypeEnumeration(String v) {
        this.value = v;
    }

    public String value() {
        return this.value;
    }

    public static DoorTypeEnumeration fromValue(String v) {
        for(DoorTypeEnumeration c : values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        throw new IllegalArgumentException(v);
    }
}

package org.rutebanken.sobek.model.vehicle;

public enum StaffingEnumeration {
    FULL_TIME("fullTime"),
    PART_TIME("partTime"),
    UNMANNED("unmanned");

    private final String value;

    private StaffingEnumeration(String v) {
        this.value = v;
    }

    public String value() {
        return this.value;
    }

    public static StaffingEnumeration fromValue(String v) {
        for(StaffingEnumeration c : values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        throw new IllegalArgumentException(v);
    }
}

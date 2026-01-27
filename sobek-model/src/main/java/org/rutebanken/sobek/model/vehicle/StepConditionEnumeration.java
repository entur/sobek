package org.rutebanken.sobek.model.vehicle;

public enum StepConditionEnumeration {
    EVEN("even"),
    UNEVEN("uneven"),
    ROUGH("rough");

    private final String value;

    private StepConditionEnumeration(String v) {
        this.value = v;
    }

    public String value() {
        return this.value;
    }

    public static StepConditionEnumeration fromValue(String v) {
        for(StepConditionEnumeration c : values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        throw new IllegalArgumentException(v);
    }
}

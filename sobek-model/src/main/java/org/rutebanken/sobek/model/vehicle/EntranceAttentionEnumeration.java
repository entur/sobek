package org.rutebanken.sobek.model.vehicle;

public enum EntranceAttentionEnumeration {
    NONE("none"),
    DOORBELL("doorbell"),
    HELP_POINT("helpPoint"),
    INTERCOM("intercom"),
    OTHER("other");

    private final String value;

    private EntranceAttentionEnumeration(String v) {
        this.value = v;
    }

    public String value() {
        return this.value;
    }

    public static EntranceAttentionEnumeration fromValue(String v) {
        for(EntranceAttentionEnumeration c : values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        throw new IllegalArgumentException(v);
    }
}

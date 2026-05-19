
package org.rutebanken.sobek.model.vehicle;

public enum HybridCategoryEnumeration {
    NONCHARGEABLE("nonChargeable"),
    CHARGEABLE("chargeable");

    private final String value;

    HybridCategoryEnumeration(String v) {
        this.value = v;
    }

    public String value() {
        return this.value;
    }

    public static HybridCategoryEnumeration fromValue(String v) {
        for(HybridCategoryEnumeration c : values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        throw new IllegalArgumentException(v);
    }
}

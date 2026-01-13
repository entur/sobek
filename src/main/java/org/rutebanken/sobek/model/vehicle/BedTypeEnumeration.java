package org.rutebanken.sobek.model.vehicle;


public enum BedTypeEnumeration {
    SINGLE_BED("singleBed"),
    DOUBLE_BED("doubleBed"),
    BED_FOR_CHILD("bedForChild"),
    COT("cot"),
    BOTTOM_BUNK("bottomBunk"),
    MIDDLE_BUNK("middleBunk"),
    TOP_BUNK("topBunk"),
    HAMMOCK("hammock"),
    OTHER("other");

    private final String value;

    private BedTypeEnumeration(String v) {
        this.value = v;
    }

    public String value() {
        return this.value;
    }

    public static BedTypeEnumeration fromValue(String v) {
        for(BedTypeEnumeration c : values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        throw new IllegalArgumentException(v);
    }
}

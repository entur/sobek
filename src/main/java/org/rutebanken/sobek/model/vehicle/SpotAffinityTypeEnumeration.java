package org.rutebanken.sobek.model.vehicle;

import jakarta.xml.bind.annotation.XmlEnumValue;

public enum SpotAffinityTypeEnumeration {
    FACE_TO_FACE("faceToFace"),
    SIDE_BY_SIDE("sideBySide"),
    CONTIGUOUS_ROW("contiguousRow"),
    SHARED_TABLE("sharedTable"),
    SEAT_BLOCK("seatBlock"),
    LOWER_BERTHS("lowerBerths"),
    SHARED_COMPARTMENT("sharedCompartment"),
    WHEELCHAIR_COMPANION_SEAT("wheelchairCompanionSeat"),
    OTHER("other");

    private final String value;

    private SpotAffinityTypeEnumeration(String v) {
        this.value = v;
    }

    public String value() {
        return this.value;
    }

    public static SpotAffinityTypeEnumeration fromValue(String v) {
        for(SpotAffinityTypeEnumeration c : values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        throw new IllegalArgumentException(v);
    }
}

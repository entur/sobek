package org.rutebanken.sobek.model.vehicle;

public enum LuggageSpotTypeEnumeration {
    RACK_ABOVE_SEATS("rackAboveSeats"),
    SPACE_UNDER_SEAT("spaceUnderSeat"),
    LUGGAGE_BAY("luggageBay"),
    LUGGAGE_COMPARTMENT("luggageCompartment"),
    LUGGAGE_VAN("luggageVan"),
    CYCLE_RACK("cycleRack"),
    OTHER("other");

    private final String value;

    private LuggageSpotTypeEnumeration(String v) {
        this.value = v;
    }

    public String value() {
        return this.value;
    }

    public static LuggageSpotTypeEnumeration fromValue(String v) {
        for(LuggageSpotTypeEnumeration c : values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        throw new IllegalArgumentException(v);
    }
}

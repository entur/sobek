package org.rutebanken.sobek.model.vehicle;

/**
 * The kind of a {@link TrainElement}. Corresponds to the NeTEx TrainElementTypeTypeEnumeration.
 */
public enum TrainElementTypeEnumeration {
    BUFFET_CAR("buffetCar"),
    CARRIAGE("carriage"),
    ENGINE("engine"),
    CAR_TRANSPORTER("carTransporter"),
    LARGE_VEHICLE_TRANSPORTER("largeVehicleTransporter"),
    SLEEPER_CARRIAGE("sleeperCarriage"),
    LUGGAGE_VAN("luggageVan"),
    RESTAURANT_CARRIAGE("restaurantCarriage"),
    OTHER("other");

    private final String value;

    TrainElementTypeEnumeration(String v) {
        this.value = v;
    }

    public String value() {
        return this.value;
    }

    public static TrainElementTypeEnumeration fromValue(String v) {
        for (TrainElementTypeEnumeration c : values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}

package org.rutebanken.sobek.netex.id;

/**
 * A vehicle model has no id of its own in Sobek: it is kept in its vehicle type, and the NeTEx vehicle model of a type is
 * given an id derived from the id of the type. Vehicle types, trains, compound trains and train elements share one id
 * sequence, so the ids of their models do not clash. {@code NMR:VehicleType:12} has the model {@code NMR:VehicleModel:12}.
 */
public final class VehicleModelId {

    private static final String MODEL_TYPE = "VehicleModel";

    private VehicleModelId() {
    }

    /**
     * The id of the vehicle model of the vehicle type with the given id.
     *
     * @throws IllegalArgumentException if the id is not of the form codespace:type:number
     */
    public static String forVehicleType(String vehicleTypeNetexId) {
        if (vehicleTypeNetexId == null) {
            throw new IllegalArgumentException("A vehicle type without an id has no vehicle model id");
        }
        int first = vehicleTypeNetexId.indexOf(':');
        int last = vehicleTypeNetexId.lastIndexOf(':');
        if (first < 0 || first == last) {
            throw new IllegalArgumentException("Not a NeTEx id, of the form codespace:type:number: " + vehicleTypeNetexId);
        }
        return vehicleTypeNetexId.substring(0, first) + ":" + MODEL_TYPE + ":" + vehicleTypeNetexId.substring(last + 1);
    }
}

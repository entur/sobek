package org.rutebanken.sobek.rest.graphql;

import org.hibernate.Hibernate;
import org.rutebanken.sobek.model.vehicle.CompoundTrain;
import org.rutebanken.sobek.model.vehicle.Train;
import org.rutebanken.sobek.model.vehicle.TrainElement;
import org.rutebanken.sobek.model.vehicle.VehicleType;

/**
 * The GraphQL name of the subtype of a vehicle type, as in the enum VehicleTypeSubtype of the schema.
 */
public enum VehicleTypeSubtype {
    VEHICLE_TYPE(VehicleType.class),
    TRAIN(Train.class),
    COMPOUND_TRAIN(CompoundTrain.class),
    TRAIN_ELEMENT(TrainElement.class);

    private final Class<? extends VehicleType> entityClass;

    VehicleTypeSubtype(Class<? extends VehicleType> entityClass) {
        this.entityClass = entityClass;
    }

    public Class<? extends VehicleType> entityClass() {
        return entityClass;
    }

    /** The subtype of a vehicle type, also when it is a lazy proxy. */
    public static VehicleTypeSubtype of(VehicleType vehicleType) {
        Object real = Hibernate.unproxy(vehicleType);
        if (real instanceof Train) {
            return TRAIN;
        }
        if (real instanceof CompoundTrain) {
            return COMPOUND_TRAIN;
        }
        if (real instanceof TrainElement) {
            return TRAIN_ELEMENT;
        }
        return VEHICLE_TYPE;
    }
}

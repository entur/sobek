package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

/**
 * A single car or engine that trains are made of (a carriage, a buffet car, an engine ...). Stored in the vehicle_type
 * table. Corresponds to NeTEx TrainElement, whose size, capacity and equipment fields are the ones of a vehicle type:
 * only the kind of element is of its own.
 * <p>
 * A {@link TrainComponent} points at the train element it places in a train.
 */
@Entity
@Getter
@Setter
public class TrainElement extends VehicleType {

    @Enumerated(EnumType.STRING)
    private TrainElementTypeEnumeration trainElementType;
}

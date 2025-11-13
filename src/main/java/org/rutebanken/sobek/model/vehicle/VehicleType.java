package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.rutebanken.sobek.model.EntityInVersionStructure;
import org.rutebanken.sobek.model.Value;

@Entity
@Getter
@Setter
public class VehicleType extends VehicleType_VersionStructure {
    @OneToOne(cascade = CascadeType.ALL)
    private PassengerCapacity passengerCapacity;

    @Override
    public void mergeWithExistingVersion(EntityInVersionStructure existingVersion) {
        if(existingVersion instanceof VehicleType) {
            if (((VehicleType) existingVersion).getKeyValues() != null) {
                ((VehicleType) existingVersion).getKeyValues().forEach((key, value) -> {
                    this.getKeyValues().put(key, new Value(value.getItems().stream().toList()));
                });
            }
        }
    }
}

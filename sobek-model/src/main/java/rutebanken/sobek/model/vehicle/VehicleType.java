package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.rutebanken.sobek.model.EntityInVersionStructure;
import org.rutebanken.sobek.model.Value;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class VehicleType extends VehicleType_VersionStructure {
    @OneToOne(cascade = CascadeType.ALL)
    private PassengerCapacity passengerCapacity;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "transportType")
    private List<Vehicle> vehicles;

    @Override
    public void mergeWithExistingVersion(EntityInVersionStructure existingVersion) {
        if(existingVersion instanceof VehicleType vtExisting) {
            if (vtExisting.getKeyValues() != null) {
                vtExisting.getKeyValues().forEach((key, value) -> {
                    this.getKeyValues().put(key, new Value(value.getItems().stream().toList()));
                });
            }

        }
    }
}

package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.Entity;
import org.rutebanken.sobek.model.EntityInVersionStructure;
import org.rutebanken.sobek.model.Value;

@Entity
public class VehicleModel extends VehicleModel_VersionStructure {
    @Override
    public void mergeWithExistingVersion(EntityInVersionStructure existingVersion) {
        if(existingVersion instanceof VehicleModel) {
            if (((VehicleModel) existingVersion).getKeyValues() != null) {
                ((VehicleModel) existingVersion).getKeyValues().forEach((key, value) -> {
                    this.getKeyValues().put(key, new Value(value.getItems().stream().toList()));
                });
            }
        }
    }

}

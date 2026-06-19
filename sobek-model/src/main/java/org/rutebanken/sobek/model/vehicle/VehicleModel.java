package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.Entity;
import org.rutebanken.sobek.model.EntityInVersionStructure;

@Entity
public class VehicleModel extends VehicleModel_VersionStructure {
    @Override
    public void mergeWithExistingVersion(EntityInVersionStructure existingVersion) {
        if(existingVersion instanceof VehicleModel vmExisting) {
            mergeKeyValues(vmExisting);
        }
    }

}

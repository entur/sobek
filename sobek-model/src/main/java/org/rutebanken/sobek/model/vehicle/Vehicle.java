package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.Entity;
import org.rutebanken.sobek.model.EntityInVersionStructure;

@Entity
public class Vehicle extends Vehicle_VersionStructure {
    @Override
    public void mergeWithExistingVersion(EntityInVersionStructure existingVersion) {
        if(existingVersion instanceof Vehicle vExisting) {
            mergeKeyValues(vExisting);
        }
    }
}

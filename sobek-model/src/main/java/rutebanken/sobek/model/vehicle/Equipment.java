package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.Getter;
import lombok.Setter;
import org.rutebanken.sobek.model.EntityInVersionStructure;
import org.rutebanken.sobek.model.Equipment_VersionStructure;
import org.rutebanken.sobek.model.Value;

@Entity
@Getter
@Setter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Equipment extends Equipment_VersionStructure {

    @Override
    public void mergeWithExistingVersion(EntityInVersionStructure existingVersion) {
        if(existingVersion instanceof Equipment vtExisting) {
            if (vtExisting.getKeyValues() != null) {
                vtExisting.getKeyValues().forEach((key, value) -> {
                    this.getKeyValues().put(key, new Value(value.getItems().stream().toList()));
                });
            }

        }
    }
}

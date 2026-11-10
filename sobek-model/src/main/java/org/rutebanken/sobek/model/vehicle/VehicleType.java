package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.rutebanken.sobek.model.EntityInVersionStructure;

import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Getter
@Setter
public class VehicleType extends VehicleType_VersionStructure {

    /**
     * Physical or contractual. Required when creating; fixed after creation. Null on incoming data means
     * "not specified", which the saver accepts only for updates (it inherits the existing version's kind).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private VehicleTypeKindEnumeration vehicleTypeKind;

    @OneToOne(cascade = CascadeType.ALL)
    private PassengerCapacity passengerCapacity;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "transportType")
    private List<Vehicle> vehicles;

    @Override
    public void mergeWithExistingVersion(EntityInVersionStructure existingVersion) {
        if(existingVersion instanceof VehicleType vtExisting) {
            if (vtExisting.getKeyValues() != null && !vtExisting.getKeyValues().isEmpty()) {
                mergeKeyValues(vtExisting);
            }
        }
    }
}

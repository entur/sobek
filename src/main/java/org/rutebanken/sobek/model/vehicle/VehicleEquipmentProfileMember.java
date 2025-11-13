package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import jakarta.xml.bind.JAXBElement;
import lombok.Getter;
import lombok.Setter;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;
import org.rutebanken.sobek.model.VersionedChildStructure;

import java.math.BigInteger;

@Entity
@Getter
@Setter
public class VehicleEquipmentProfileMember extends VersionedChildStructure {
    @Transient
    protected EmbeddableMultilingualString name;
    @Transient
    protected EmbeddableMultilingualString description;

    protected String equipmentRef;
    protected BigInteger minimumUnits;

}

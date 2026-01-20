package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import org.rutebanken.sobek.model.DataManagedObjectStructure;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;

import java.math.BigInteger;
import java.util.List;

@MappedSuperclass
@Getter
@Setter
public class VehicleEquipmentProfile_VersionStructure extends DataManagedObjectStructure {
    @Transient
    private EmbeddableMultilingualString name;
    @Transient
    private EmbeddableMultilingualString description;

//    private JAXBElement<? extends EquipmentRefStructure> equipmentRef;

    @Transient
    private BigInteger units;
    @Transient
    private EmbeddableMultilingualString manufacturer;
//    private TypeOfEquipmentRefStructure typeOfEquipmentRef;
//    private PurposeOfEquipmentProfileRefStructure purposeOfEquipmentProfileRef;

    @OneToMany(cascade = jakarta.persistence.CascadeType.ALL)
    private List<VehicleEquipmentProfileMember> vehicleEquipmentProfileMembers;

}

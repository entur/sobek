package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.*;
import jakarta.xml.bind.JAXBElement;
import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import org.rutebanken.sobek.model.DataManagedObjectStructure;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;

@MappedSuperclass
@Getter
@Setter
public class VehicleModel_VersionStructure extends DataManagedObjectStructure {
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "name_value")),
            @AttributeOverride(name = "lang", column = @Column(name = "name_lang", length = 5))
    })
    @Embedded
    private EmbeddableMultilingualString name;
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "description_value")),
            @AttributeOverride(name = "lang", column = @Column(name = "description_lang", length = 5))
    })
    @Embedded
    private EmbeddableMultilingualString description;
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "manufacturer_value")),
            @AttributeOverride(name = "lang", column = @Column(name = "manufacturer_lang", length = 5))
    })
    @Embedded
    private EmbeddableMultilingualString manufacturer;

    @ManyToOne
    private VehicleType transportType;

    private BigDecimal range;
    private BigDecimal fullCharge;

    @Transient
    private VehicleEquipmentProfileRefs_RelStructure equipmentProfiles;
    @Transient
    private JAXBElement<? extends VehicleModelProfileRefStructure> vehicleModelProfileRef;
//    private ContactStructure customerServiceContactDetails;

}

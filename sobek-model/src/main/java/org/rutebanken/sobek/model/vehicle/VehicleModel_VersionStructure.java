package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
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

    private String transportTypeRef;
    private BigDecimal range;
    private BigDecimal fullCharge;
    @Transient
    private VehicleEquipmentProfileRefs_RelStructure equipmentProfiles;
//    @Transient
//    private JAXBElement<? extends VehicleModelProfileRefStructure> vehicleModelProfileRef;
//    private ContactStructure customerServiceContactDetails;

}

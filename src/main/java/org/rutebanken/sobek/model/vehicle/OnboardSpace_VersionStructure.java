package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.rutebanken.netex.model.ActualVehicleEquipments_RelStructure;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;
import org.rutebanken.sobek.model.Zone_VersionStructure;

import java.math.BigDecimal;
import java.util.List;

@MappedSuperclass
@Getter
@Setter
public abstract class OnboardSpace_VersionStructure extends Zone_VersionStructure {
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "label_value")),
            @AttributeOverride(name = "lang", column = @Column(name = "label_lang", length = 5))
    })
    @Embedded
    private EmbeddableMultilingualString label;

    @Enumerated(EnumType.STRING)
    private ComponentOrientationEnumeration orientation;
    private BigDecimal width;
    private BigDecimal length;
    private BigDecimal height;

//    private JAXBElement<? extends FacilitySetRefStructure> facilitySetRef;
    @OneToMany(cascade = CascadeType.MERGE, orphanRemoval = true)
    protected List<Equipment> actualVehicleEquipments;
}

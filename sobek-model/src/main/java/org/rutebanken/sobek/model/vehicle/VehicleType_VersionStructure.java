package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;

import java.math.BigDecimal;

@MappedSuperclass
@Getter
@Setter
public class VehicleType_VersionStructure extends TransportType_VersionStructure {

    private Boolean lowFloor;
    private Boolean hasLiftOrRamp;
    private Boolean hasHoist;
    private BigDecimal hoistOperatingRadius;
    private BigDecimal boardingHeight;
    private BigDecimal gapToPlatform;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private BigDecimal weight;
    private BigDecimal firstAxleHeight;
    private Boolean monitored;
    @Transient
    private VehicleTypeRefStructure includedIn;

    private BigDecimal formDragCoefficient;
    private BigDecimal rollResistanceCoefficient;
    private BigDecimal maximumEngineEffectKW;
    @Enumerated(EnumType.STRING)
    private HybridCategoryEnumeration hybridCategory;

    /**
     * What NeTEx keeps in a vehicle model: a vehicle type has at most one, and it is stored with the type. The model
     * exists in NeTEx only, as an element derived from the type on export and read into the type on import.
     */
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "manufacturer_value")),
            @AttributeOverride(name = "lang", column = @Column(name = "manufacturer_lang", length = 5))
    })
    @Embedded
    private EmbeddableMultilingualString manufacturer;
    private BigDecimal range;
    private BigDecimal fullCharge;

    @Transient
    private VehicleModelRefStructure classifiedAsRef;
    @Transient
    private ServiceFacilitySets_RelStructure facilities;
    @Transient
    private PassengerCarryingRequirements_RelStructure canCarry;
    @Transient
    private VehicleManoeuvringRequirements_RelStructure canManoeuvre;
    @Transient
    private FacilityRequirements_RelStructure satisfiesFacilityRequirements;

    /** True if the type has anything of what NeTEx keeps in a vehicle model. */
    public boolean hasVehicleModelData() {
        return manufacturer != null || range != null || fullCharge != null;
    }

}

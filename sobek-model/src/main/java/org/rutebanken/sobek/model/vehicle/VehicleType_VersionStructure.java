package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

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
    private HybridCategoryEnumeration hybridCategory;

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

}

package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;

@Getter
@Setter
@MappedSuperclass
public class AccessVehicleEquipment_VersionStructure extends ActualVehicleEquipment_VersionStructure {
    private Boolean lowFloor;
    private Boolean highFloor;
    private Boolean hoist;
    private BigDecimal hoistOperatingRadius;
    private Boolean ramp;
    private BigDecimal bearingCapacity;
    private BigInteger numberOfSteps;
    private BigDecimal boardingHeight;
    private BigDecimal equipmentLength;
    private BigDecimal equipmentWidth;
    private BigDecimal gapToPlatform;
    private BigDecimal widthOfAccessArea;
    private BigDecimal heightOfAccessArea;
    private Boolean automaticDoors;
//    private List<MobilityEnumeration> suitableFor;
//    private AssistanceNeededEnumeration assistanceNeeded;
//    private AssistedBoardingLocationEnumeration assistedBoardingLocation;
    private Boolean guideDogsAllowed;
}
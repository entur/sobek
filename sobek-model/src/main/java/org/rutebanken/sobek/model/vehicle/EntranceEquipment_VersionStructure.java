package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;

@Getter
@Setter
@MappedSuperclass
public class EntranceEquipment_VersionStructure extends AccessEquipment_VersionStructure {
    private Boolean door;
    private CompassBearing8Enumeration doorOrientation;
    private DoorHandleEnumeration doorHandleOutside;
    private DoorHandleEnumeration doorHandleInside;
    private Boolean revolvingDoor;
    private DoorTypeEnumeration doorType;
    private BigInteger numberOfGates;
    private StaffingEnumeration staffing;
    private Boolean entranceRequiresStaffing;
    private Boolean entranceRequiresTicket;
    private Boolean entranceRequiresPassport;
    private Boolean dropKerbOutside;
    private Boolean acousticSensor;
    private Boolean automaticDoor;
    private BigDecimal doorControlElementHeight;
    private Boolean glassDoor;
    private Boolean airlock;
    private Boolean wheelchairPassable;
    private Boolean wheelchairUnaided;
    private Boolean audioOrVideoIntercom;
    private EntranceAttentionEnumeration entranceAttention;
    private Boolean doorstepMark;
    private NecessaryForceEnumeration necessaryForceToOpen;
    private Boolean suitableForCycles;
    private Boolean audioPassthroughIndicator;
    private Boolean rampDoorbell;
    private Boolean recognizable;
    private EntranceTurningSpacePositionEnumeration turningSpacePosition;
    private BigDecimal wheelchairTurningCircle;
}

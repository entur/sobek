package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;
import java.math.BigInteger;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class PassengerEntrance extends DeckComponent_VersionStructure {
    @Enumerated(EnumType.STRING)
    private VehicleSideEnumeration vehicleSide;

    private BigDecimal distanceFromFront;
    private BigInteger sequenceFromFront;
    private BigDecimal heightFromGround;

    @Enumerated(EnumType.STRING)
    private DeckEntranceTypeEnumeration deckEntranceType;
    private Boolean hasDoor;
    private Boolean isAutomatic;
    private Boolean isEmergencyExit;

    //    private SensorsInEntrance_RelStructure sensorsInEntrance;
//    private TypeOfDeckEntranceUsageRefStructure typeOfDeckEntranceUsageRef;
}

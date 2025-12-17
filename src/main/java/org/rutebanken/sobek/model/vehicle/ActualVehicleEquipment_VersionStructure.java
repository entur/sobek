package org.rutebanken.sobek.model.vehicle;

import lombok.Getter;
import lombok.Setter;
import org.rutebanken.sobek.model.AccessibilityAssessment;

import java.math.BigInteger;

@Getter
@Setter
public class ActualVehicleEquipment_VersionStructure extends PassengerEquipment_VersionStructure {
    private BigInteger units;
//    private JAXBElement<? extends org.rutebanken.netex.model.VehicleTypeRefStructure> vehicleTypeRef;
//    private JAXBElement<? extends EquipmentRefStructure> equipmentRef;
//    private AccessibilityAssessment accessibilityAssessment;
}
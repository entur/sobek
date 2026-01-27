package org.rutebanken.sobek.model.vehicle;

import jakarta.xml.bind.JAXBElement;

import java.util.ArrayList;
import java.util.List;
import org.rutebanken.sobek.model.OneToManyRelationshipStructure;

public class VehicleEquipmentProfileRefs_RelStructure extends OneToManyRelationshipStructure {

    private List<JAXBElement<? extends VehicleEquipmentProfileRefStructure>> vehicleEquipmentProfileRef;

    public List<JAXBElement<? extends VehicleEquipmentProfileRefStructure>> getVehicleEquipmentProfileRef() {
        if (this.vehicleEquipmentProfileRef == null) {
            this.vehicleEquipmentProfileRef = new ArrayList();
        }

        return this.vehicleEquipmentProfileRef;
    }
}
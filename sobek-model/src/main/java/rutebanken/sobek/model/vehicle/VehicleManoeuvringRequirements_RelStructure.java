package org.rutebanken.sobek.model.vehicle;

import org.rutebanken.sobek.model.ContainmentAggregationStructure;

import java.util.ArrayList;
import java.util.List;

public class VehicleManoeuvringRequirements_RelStructure extends ContainmentAggregationStructure {

    private List<Object> vehicleManoeuvringRequirementRefOrVehicleManoeuvringRequirement;

    public List<Object> getVehicleManoeuvringRequirementRefOrVehicleManoeuvringRequirement() {
        if (this.vehicleManoeuvringRequirementRefOrVehicleManoeuvringRequirement == null) {
            this.vehicleManoeuvringRequirementRefOrVehicleManoeuvringRequirement = new ArrayList();
        }

        return this.vehicleManoeuvringRequirementRefOrVehicleManoeuvringRequirement;
    }
}

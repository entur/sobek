package org.rutebanken.sobek.model.vehicle;

import org.rutebanken.sobek.model.ContainmentAggregationStructure;

import java.util.ArrayList;
import java.util.List;

public class PassengerCarryingRequirements_RelStructure extends ContainmentAggregationStructure {

    private List<Object> passengerCarryingRequirementRefOrPassengerCarryingRequirement;

    public List<Object> getPassengerCarryingRequirementRefOrPassengerCarryingRequirement() {
        if (this.passengerCarryingRequirementRefOrPassengerCarryingRequirement == null) {
            this.passengerCarryingRequirementRefOrPassengerCarryingRequirement = new ArrayList();
        }

        return this.passengerCarryingRequirementRefOrPassengerCarryingRequirement;
    }
}

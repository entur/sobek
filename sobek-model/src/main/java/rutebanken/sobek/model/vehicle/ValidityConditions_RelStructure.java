package org.rutebanken.sobek.model.vehicle;

import org.rutebanken.sobek.model.ContainmentAggregationStructure;

import java.util.ArrayList;
import java.util.List;

public class ValidityConditions_RelStructure extends ContainmentAggregationStructure {

    private List<Object> validityConditionRefOrValidBetweenOrValidityCondition_Dummy;

    public List<Object> getValidityConditionRefOrValidBetweenOrValidityCondition_Dummy() {
        if (this.validityConditionRefOrValidBetweenOrValidityCondition_Dummy == null) {
            this.validityConditionRefOrValidBetweenOrValidityCondition_Dummy = new ArrayList();
        }

        return this.validityConditionRefOrValidBetweenOrValidityCondition_Dummy;
    }
}

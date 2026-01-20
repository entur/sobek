package org.rutebanken.sobek.model.vehicle;

import org.rutebanken.sobek.model.ContainmentAggregationStructure;

import java.util.ArrayList;
import java.util.List;

public class TrainComponents_RelStructure extends ContainmentAggregationStructure {

    private List<Object> trainComponentRefOrTrainComponent;

    public List<Object> getTrainComponentRefOrTrainComponent() {
        if (this.trainComponentRefOrTrainComponent == null) {
            this.trainComponentRefOrTrainComponent = new ArrayList();
        }

        return this.trainComponentRefOrTrainComponent;
    }
}

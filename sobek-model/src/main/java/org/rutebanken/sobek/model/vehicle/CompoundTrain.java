package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * A train made up of other trains (or train element types). Stored in the vehicle_type table.
 * Corresponds to NeTEx CompoundTrain.
 */
@Entity
@Getter
@Setter
public class CompoundTrain extends VehicleType {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "owner_compound_train_id")
    @OrderColumn(name = "component_order")
    private List<TrainInCompoundTrain> components = new ArrayList<>();
}

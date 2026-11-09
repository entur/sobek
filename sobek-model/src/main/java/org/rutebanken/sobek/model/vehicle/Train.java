package org.rutebanken.sobek.model.vehicle;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * A train (unit) built from an ordered list of components. Stored in the vehicle_type table.
 * Corresponds to NeTEx Train, whose size is the flattened TrainSize structure.
 */
@Entity
@Getter
@Setter
public class Train extends VehicleType {

    private Integer numberOfCars;

    @Enumerated(EnumType.STRING)
    private TrainSizeEnumeration trainSizeType;

    // Eager: GraphQL and the exports read the components after the query that loaded the vehicle type has ended.
    // SUBSELECT loads the components of all vehicle types of a query in one extra select.
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @JoinColumn(name = "owner_train_id")
    @OrderColumn(name = "component_order")
    private List<TrainComponent> components = new ArrayList<>();
}

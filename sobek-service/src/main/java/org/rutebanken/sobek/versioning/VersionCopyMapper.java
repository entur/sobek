package org.rutebanken.sobek.versioning;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.SubclassMapping;
import org.rutebanken.sobek.model.vehicle.CompoundTrain;
import org.rutebanken.sobek.model.vehicle.DeckPlan;
import org.rutebanken.sobek.model.vehicle.PassengerCapacity;
import org.rutebanken.sobek.model.vehicle.Train;
import org.rutebanken.sobek.model.vehicle.TrainComponent;
import org.rutebanken.sobek.model.vehicle.TrainInCompoundTrain;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(
        config = SobekMapperConfig.class)
public interface VersionCopyMapper {

    /** The transport type is not copied: the copy keeps referring to the same (possibly Train or CompoundTrain) row. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "transportType", ignore = true)
    Vehicle copy(Vehicle source);

    /**
     * Copies the vehicle type as the same subtype as the source. Without the subclass mappings, a Train or
     * CompoundTrain would be sliced into a plain VehicleType and lose its components.
     */
    @SubclassMapping(source = Train.class, target = Train.class)
    @SubclassMapping(source = CompoundTrain.class, target = CompoundTrain.class)
    @Mapping(target = "id", ignore = true)
    VehicleType copy(VehicleType source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicles", ignore = true)
    @Mapping(target = "components", qualifiedByName = "copyTrainComponents")
    Train copy(Train source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicles", ignore = true)
    @Mapping(target = "components", qualifiedByName = "copyTrainInCompoundTrains")
    CompoundTrain copy(CompoundTrain source);

    @Mapping(target = "id", ignore = true)
    TrainComponent copy(TrainComponent source);

    /** A copied component points at the same train as the original: only the owner is versioned. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "train", qualifiedByName = "sameTrain")
    TrainInCompoundTrain copy(TrainInCompoundTrain source);

    /**
     * MapStruct copies a list of an unchanged element type as a list of the same elements. The components belong to
     * one version of their owner, so each version needs its own component rows.
     */
    @Named("copyTrainComponents")
    default List<TrainComponent> copyTrainComponents(List<TrainComponent> components) {
        return components == null ? null : components.stream().map(this::copy).collect(Collectors.toCollection(ArrayList::new));
    }

    @Named("copyTrainInCompoundTrains")
    default List<TrainInCompoundTrain> copyTrainInCompoundTrains(List<TrainInCompoundTrain> components) {
        return components == null ? null : components.stream().map(this::copy).collect(Collectors.toCollection(ArrayList::new));
    }

    @Named("sameTrain")
    default Train sameTrain(Train train) {
        return train;
    }

    @Mapping(target = "id", ignore = true)
    DeckPlan copy(DeckPlan source);

    @Mapping(target = "id", ignore = true)
    PassengerCapacity copy(PassengerCapacity source);
}

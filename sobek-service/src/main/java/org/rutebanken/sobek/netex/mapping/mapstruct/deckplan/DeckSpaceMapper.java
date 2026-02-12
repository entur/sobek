package org.rutebanken.sobek.netex.mapping.mapstruct.deckplan;

import org.mapstruct.*;
import org.rutebanken.netex.model.*;
import org.rutebanken.netex.model.ObjectFactory;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.*;
import org.rutebanken.sobek.netex.mapping.mapstruct.equipment.ActualVehicleEquipmentMapper;

import java.util.List;

/**
 * MapStruct mapper for DeckSpace.
 * Handles mapping between NeTEx DeckSpace and Sobek DeckSpace entity.
 */
@Mapper(
        config = SobekMapperConfig.class,
        uses = {
                DataManagedObjectStructureMapper.class,
                PolygonMapper.class,
                PointRefStructureMapper.class,
                SimplePointMapper.class,
                PassengerEntranceMapper.class,
                DeckSpaceCapacityMapper.class,
                PassengerSpotMapper.class,
                LuggageSpotMapper.class,
                ActualVehicleEquipmentMapper.class
        }
)
public interface DeckSpaceMapper {

    ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    /**
     * Maps from NeTEx DeckSpace to Sobek entity.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    @Mapping(target = "polygon", source = "polygon", qualifiedByName = "polygonTypeToPolygon")
    @Mapping(target = "incomingId", source = "id")
    @Mapping(target = "spotAffinities", ignore = true) // Handled by AfterMapping.
    @Mapping(target = "parentDeckSpace", ignore = true) // Handled by AfterMapping.
    org.rutebanken.sobek.model.vehicle.PassengerSpace mapToSobek(
            PassengerSpace source,
            @Context MappingContext context
    );

    /**
     * Maps from Sobek entity back to NeTEx DeckSpace.
     */
    @DataManagedObjectStructureMapper.ToNetexMappings
    @Mapping(target = "polygon", source = "polygon", qualifiedByName = "polygonToPolygonType")
    @Mapping(target = "typeOfDeckSpaceRef", ignore = true) // TODO: Implement when needed
    @Mapping(target = "deckEntranceCouples", ignore = true) // TODO: Implement when needed
    @Mapping(target = "deckEntranceUsages", ignore = true) // TODO: Implement when needed
    @Mapping(target = "deckWindows", ignore = true) // TODO: Implement when needed
    @Mapping(target = "spotAffinities", ignore = true) // Handled by AfterMapping.
    @Mapping(target = "parentDeckSpaceRef", ignore = true) // Handled by AfterMapping.
    PassengerSpace mapToNetex(
            org.rutebanken.sobek.model.vehicle.PassengerSpace source,
            @Context MappingContext context
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    @Mapping(target = "polygon", source = "polygon", qualifiedByName = "polygonTypeToPolygon")
    @Mapping(target = "incomingId", source = "id")
    @Mapping(target = "spotAffinities", ignore = true) // Handled by AfterMapping.
    @Mapping(target = "parentDeckSpace", ignore = true) // Handled by AfterMapping.
    void updateSobekFromNetex(
            PassengerSpace source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.PassengerSpace target,
            @Context MappingContext context
    );

    @AfterMapping
    default void afterMapToSobek(PassengerSpace source,
                                 @MappingTarget org.rutebanken.sobek.model.vehicle.PassengerSpace target,
                                 @Context MappingContext context) {
        context.setCurrentSobekDeckSpace(target);
        target.setSpotAffinities(context.getSpotAffinityMapper().mapListToSobek(source.getSpotAffinities(), context));
    }

    @AfterMapping
    default void afterMapToNetex(org.rutebanken.sobek.model.vehicle.PassengerSpace source,
                                 @MappingTarget PassengerSpace target,
                                 @Context MappingContext context) {
        target.setSpotAffinities(context.getSpotAffinityMapper().mapListToNetex(source.getSpotAffinities(), context));
        if(source.getParentDeckSpace() != null) {
            target.setParentDeckSpaceRef(new DeckSpaceRefStructure().withRef(source.getParentDeckSpace().getNetexId()));
        }
    }

    /**
     * Maps a list of DeckSpaces from Sobek to NeTEx RelStructure.
     */
    default DeckSpaces_RelStructure mapSobekListToNetexRelStructure(
            List<org.rutebanken.sobek.model.vehicle.PassengerSpace> source,
            @Context MappingContext context
    ) {
        if (source == null) {
            return null;
        }

        if (source.isEmpty()) {
            return new DeckSpaces_RelStructure();
        }

        return new DeckSpaces_RelStructure().withDeckSpaceRefOrDeckSpace_Dummy(source.stream()
                .map(sobekDeckSpace -> OBJECT_FACTORY.createPassengerSpace(mapToNetex(sobekDeckSpace, context)))
                .collect(java.util.stream.Collectors.toList()));
    }

    /**
     * Maps a list of DeckSpaces from NeTEx RelStructure to Sobek.
     */
    default List<org.rutebanken.sobek.model.vehicle.PassengerSpace> mapNetexRelStructureToSobekList(
            DeckSpaces_RelStructure source,
            @Context MappingContext context
    ) {
        if (source == null || source.getDeckSpaceRefOrDeckSpace_Dummy() == null) {
            return null;
        }

        var rawNetexList = source.getDeckSpaceRefOrDeckSpace_Dummy()
                .stream()
                .map(ds -> (PassengerSpace) ds.getValue())
                .toList();

        // At this time, we can't map the mapping from child to parent, because the objects don't have an id yet.
        var sobekList =  rawNetexList.stream()
                .map(netexDeckSpace -> mapToSobek(netexDeckSpace, context)) // TODO: Each item could be either PassengerSpace or DeckSpaceRef
                .toList();

        // Find the deck spaces with a parent.
        var netexWithParent = rawNetexList
                .stream()
                .filter(ps-> ps.getParentDeckSpaceRef() != null)
                .toList();

        // Set the parent in the Sobek deckspace for each netex deck space with a parent.
        netexWithParent.forEach(ps -> {
            var sobekParent = sobekList.stream().filter(ds -> ds.getIncomingId().equals(ps.getParentDeckSpaceRef().getRef())).findFirst().orElse(null);
            var sobekChild = sobekList.stream().filter(ds -> ds.getIncomingId().equals(ps.getId())).findFirst().orElse(null);
            if(sobekParent != null && sobekChild != null) {
                sobekChild.setParentDeckSpace(sobekParent);
            } else {
                // TODO: Log warning that a parent was not found.
            }
        });

        return sobekList;

    }
}
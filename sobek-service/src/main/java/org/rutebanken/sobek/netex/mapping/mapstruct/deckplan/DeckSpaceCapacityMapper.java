package org.rutebanken.sobek.netex.mapping.mapstruct.deckplan;

import org.mapstruct.*;
import org.rutebanken.netex.model.DeckSpaceCapacities_RelStructure;
import org.rutebanken.netex.model.DeckSpaceCapacity;
import org.rutebanken.netex.model.DeckSpaceCapacity_VersionedChildStructure;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.EntityInVersionMapper;

import java.util.List;

/**
 * MapStruct mapper for DeckSpaceCapacity.
 * Handles mapping between NeTEx DeckSpaceCapacity and Sobek DeckSpaceCapacity entity.
 */
@Mapper(
        config = SobekMapperConfig.class,
        uses = {
                EntityInVersionMapper.class
        }
)
public interface DeckSpaceCapacityMapper {

    /**
     * Maps from NeTEx DeckSpaceCapacity to Sobek entity.
     */
    @EntityInVersionMapper.ToSobekMappings
    org.rutebanken.sobek.model.vehicle.DeckSpaceCapacity mapToSobek(
            DeckSpaceCapacity_VersionedChildStructure source,
            @Context MappingContext context
    );

    /**
     * Maps from Sobek entity back to NeTEx DeckSpaceCapacity.
     */
    @EntityInVersionMapper.ToNetexMappings
    @Mapping(target = "typeOfLocatableSpotRef", ignore = true) // TODO: Implement when needed
    DeckSpaceCapacity mapToNetex(
            org.rutebanken.sobek.model.vehicle.DeckSpaceCapacity source,
            @Context MappingContext context
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    @EntityInVersionMapper.ToSobekMappings
    void updateSobekFromNetex(
            DeckSpaceCapacity source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.DeckSpaceCapacity target,
            @Context MappingContext context
    );

    /**
     * Maps a list of DeckSpaceCapacities from Sobek to NeTEx RelStructure.
     */
    default DeckSpaceCapacities_RelStructure mapSobekListToNetexRelStructure(
            List<org.rutebanken.sobek.model.vehicle.DeckSpaceCapacity> source,
            @Context MappingContext context
    ) {
        if (source == null) {
            return null;
        }

        if (source.isEmpty()) {
            return new DeckSpaceCapacities_RelStructure();
        }

        return new DeckSpaceCapacities_RelStructure().withDeckSpaceCapacity(source.stream()
                .map(sobekCapacity -> mapToNetex(sobekCapacity, context))
                .collect(java.util.stream.Collectors.toList()));
    }

    /**
     * Maps a list of DeckSpaceCapacities from NeTEx RelStructure to Sobek.
     */
    default List<org.rutebanken.sobek.model.vehicle.DeckSpaceCapacity> mapNetexRelStructureToSobekList(
            DeckSpaceCapacities_RelStructure source,
            @Context MappingContext context
    ) {
        if (source == null || source.getDeckSpaceCapacity() == null) {
            return null;
        }

        return source.getDeckSpaceCapacity().stream()
                .map(netexCapacity -> mapToSobek(netexCapacity, context))
                .collect(java.util.stream.Collectors.toList());
    }
}
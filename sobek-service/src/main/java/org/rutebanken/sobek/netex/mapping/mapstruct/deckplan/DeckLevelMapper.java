package org.rutebanken.sobek.netex.mapping.mapstruct.deckplan;

import org.mapstruct.*;
import org.rutebanken.netex.model.DeckLevel;
import org.rutebanken.netex.model.DeckLevels_RelStructure;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for DeckLevel.
 * Handles mapping between NeTEx DeckLevel and Sobek DeckLevel entity.
 */
@Mapper(
        config = SobekMapperConfig.class,
        uses = {DataManagedObjectStructureMapper.class, EntityInVersionMapper.class
        }
)
public interface DeckLevelMapper {

    /**
     * Maps from NeTEx DeckLevel to Sobek entity.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    org.rutebanken.sobek.model.vehicle.DeckLevel mapToSobek(
            DeckLevel source,
            @Context MappingContext context
    );

    /**
     * Maps from Sobek entity back to NeTEx DeckLevel.
     */
    @DataManagedObjectStructureMapper.ToNetexMappings
    DeckLevel mapToNetex(
            org.rutebanken.sobek.model.vehicle.DeckLevel source,
            @Context MappingContext context
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    void updateSobekFromNetex(
            DeckLevel source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.DeckLevel target,
            @Context MappingContext context
    );

    @AfterMapping
    default void afterMapToSobek(DeckLevel source,
                                 @MappingTarget org.rutebanken.sobek.model.vehicle.DeckLevel target,
                                 @Context MappingContext context) {
        if(target != null) {
            context.getDataManagedObjectStructureMapper().afterMappingToSobek(source, target, context);
        }
    }

    @AfterMapping
    default void afterMapToNetex(org.rutebanken.sobek.model.vehicle.DeckLevel source,
                                 @MappingTarget DeckLevel target,
                                 @Context MappingContext context) {
        if(target != null) {
            context.getDataManagedObjectStructureMapper().afterMappingToNetex(source, target, context);
        }
    }

    /**
     * Maps a list of Decks from Sobek to NeTEx RelStructure.
     */
    default DeckLevels_RelStructure mapSobekListToNetexRelStructure(
            List<org.rutebanken.sobek.model.vehicle.DeckLevel> source,
            @Context MappingContext context
    ) {
        if (source == null) {
            return null;
        }

        if (source.isEmpty()) {
            return new DeckLevels_RelStructure();
        }

        return new DeckLevels_RelStructure().withDeckLevel(source.stream()
                .map(sobekDeckLevel -> mapToNetex(sobekDeckLevel, context))
                .collect(java.util.stream.Collectors.toList()));
    }

    /**
     * Maps a list of Decks from NeTEx RelStructure to Sobek.
     */
    default List<org.rutebanken.sobek.model.vehicle.DeckLevel> mapNetexRelStructureToSobekList(
            DeckLevels_RelStructure source,
            @Context MappingContext context
    ) {
        if (source == null || source.getDeckLevel() == null) {
            return null;
        }

        return source.getDeckLevel().stream()
                .map(netexDeckLevel -> mapToSobek(netexDeckLevel, context))
                .collect(java.util.stream.Collectors.toList());
    }
}
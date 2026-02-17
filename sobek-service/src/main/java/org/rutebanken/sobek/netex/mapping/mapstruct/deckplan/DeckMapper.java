package org.rutebanken.sobek.netex.mapping.mapstruct.deckplan;

import org.mapstruct.*;
import org.rutebanken.netex.model.Deck;
import org.rutebanken.netex.model.Decks_RelStructure;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.PointRefStructureMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.PolygonMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.SimplePointMapper;

import java.util.List;

/**
 * MapStruct mapper for Deck.
 * Handles mapping between NeTEx Deck and Sobek Deck entity.
 */
@Mapper(
        config = SobekMapperConfig.class,
        uses = {PointRefStructureMapper.class,
                DataManagedObjectStructureMapper.class,
                PolygonMapper.class,
                SimplePointMapper.class,
                SpotRowMapper.class,
                SpotColumnMapper.class
        }
)
public interface DeckMapper {

    /**
     * Maps from NeTEx Deck to Sobek entity.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    @Mapping(target = "polygon", source = "polygon", qualifiedByName = "polygonTypeToPolygon")
    @Mapping(target = "deckSpaces", ignore = true) // Handled by DeckSpaceMapper. This is to ensure that this mapping happens AFTER SpotRowMapper and SpotColumnMapper.
    org.rutebanken.sobek.model.vehicle.Deck mapToSobek(
            Deck source,
            @Context MappingContext context
    );

    /**
     * Maps from Sobek entity back to NeTEx Deck.
     */
    @DataManagedObjectStructureMapper.ToNetexMappings
    @Mapping(target = "polygon", source = "polygon", qualifiedByName = "polygonToPolygonType")
    @Mapping(target = "deckSpaces", ignore = true) // Handled by AfterMapping. This is to ensure that this mapping happens AFTER SpotRowMapper and SpotColumnMapper.
    Deck mapToNetex(
            org.rutebanken.sobek.model.vehicle.Deck source,
            @Context MappingContext context
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    @Mapping(target = "polygon", source = "polygon", qualifiedByName = "polygonTypeToPolygon")
    @Mapping(target = "deckSpaces", ignore = true) // Handled by AfterMapping. This is to ensure that this mapping happens AFTER SpotRowMapper and SpotColumnMapper.
    void updateSobekFromNetex(
            Deck source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.Deck target,
            @Context MappingContext context
    );

    @AfterMapping
    default void afterMapToSobek(Deck source,
                                 @MappingTarget org.rutebanken.sobek.model.vehicle.Deck target,
                                 @Context MappingContext context) {
        if(target != null) {
            context.getDataManagedObjectStructureMapper().afterMappingToSobek(source, target, context);
        }
        context.setCurrentSobekDeck(target);
        target.setDeckSpaces(context.getDeckSpaceMapper().mapNetexRelStructureToSobekList(source.getDeckSpaces(), context));
    }

    @AfterMapping
    default void afterMapToNetex(org.rutebanken.sobek.model.vehicle.Deck source,
                                 @MappingTarget Deck target,
                                 @Context MappingContext context) {
        if(target != null) {
            context.getDataManagedObjectStructureMapper().afterMappingToNetex(source, target, context);
        }
        target.setDeckSpaces(context.getDeckSpaceMapper().mapSobekListToNetexRelStructure(source.getDeckSpaces(), context));
    }

    /**
     * Maps a list of Decks from Sobek to NeTEx RelStructure.
     */
    default Decks_RelStructure mapSobekListToNetexRelStructure(
            List<org.rutebanken.sobek.model.vehicle.Deck> source,
            @Context MappingContext context
    ) {
        if (source == null) {
            return null;
        }

        if (source.isEmpty()) {
            return new Decks_RelStructure();
        }

        return new Decks_RelStructure().withDeck(source.stream()
                .map(sobekDeck -> mapToNetex(sobekDeck, context))
                .collect(java.util.stream.Collectors.toList()));
    }

    /**
     * Maps a list of Decks from NeTEx RelStructure to Sobek.
     */
    default List<org.rutebanken.sobek.model.vehicle.Deck> mapNetexRelStructureToSobekList(
            Decks_RelStructure source,
            @Context MappingContext context
    ) {
        if (source == null || source.getDeck() == null) {
            return null;
        }

        return source.getDeck().stream()
                .map(netexDeck -> mapToSobek(netexDeck, context))
                .collect(java.util.stream.Collectors.toList());
    }
}
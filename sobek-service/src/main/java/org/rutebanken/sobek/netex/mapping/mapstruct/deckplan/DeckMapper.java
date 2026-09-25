package org.rutebanken.sobek.netex.mapping.mapstruct.deckplan;

import jakarta.xml.bind.JAXBElement;
import org.mapstruct.*;
import org.rutebanken.netex.model.*;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Deck.
 * Handles mapping between NeTEx Deck and Sobek Deck entity.
 */
@Mapper(
        config = SobekMapperConfig.class,
        uses = {ZoneMapper.class,
                SpotRowMapper.class,
                SpotColumnMapper.class,
                PointRefStructureMapper.class,
                EntityInVersionMapper.class,
                PolygonMapper.class
        }
)
public interface DeckMapper {

    /**
     * Maps from NeTEx Deck to Sobek entity.
     */
    @ZoneMapper.ToSobekMappings
    @Mapping(target = "deckSpaces", ignore = true) // Handled by DeckSpaceMapper. This is to ensure that this mapping happens AFTER SpotRowMapper and SpotColumnMapper.
    org.rutebanken.sobek.model.vehicle.Deck mapToSobek(
            Deck source,
            @Context MappingContext context
    );

    /**
     * Maps from Sobek entity back to NeTEx Deck.
     */
    @ZoneMapper.ToNetexMappings
    @Mapping(target = "deckSpaces", ignore = true) // Handled by AfterMapping. This is to ensure that this mapping happens AFTER SpotRowMapper and SpotColumnMapper.
    Deck mapToNetex(
            org.rutebanken.sobek.model.vehicle.Deck source,
            @Context MappingContext context
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    @ZoneMapper.ToSobekMappings
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
            context.getZoneMapper().afterMapToSobek(source, target, context);
            context.setCurrentSobekDeck(target);
            target.setDeckSpaces(context.getDeckSpaceMapper().mapNetexRelStructureToSobekList(source.getDeckSpaces(), context));
            target.setDeckLevel(mapNetexRef2Sobek(source.getDeckLevelRef(), context.getCurrentSobekDeckPlan().getDeckLevels()));
        }
    }

    @AfterMapping
    default void afterMapToNetex(org.rutebanken.sobek.model.vehicle.Deck source,
                                 @MappingTarget Deck target,
                                 @Context MappingContext context) {
        if(target != null) {
            context.getZoneMapper().afterMapToNetex(source, target, context);
            target.setDeckSpaces(context.getDeckSpaceMapper().mapSobekListToNetexRelStructure(source.getDeckSpaces(), context));
            target.setDeckLevelRef(mapToNetexRef(source.getDeckLevel()));
        }
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


    default org.rutebanken.sobek.model.vehicle.DeckLevel mapNetexRef2Sobek(DeckLevelRefStructure deckLevelRefStructure, List<org.rutebanken.sobek.model.vehicle.DeckLevel> deckLevels) {
        if (deckLevelRefStructure == null || deckLevelRefStructure.getRef() == null || deckLevelRefStructure.getRef().isEmpty()) {
            return null;
        }

        if(deckLevels == null) {
            return null;
        }
        return ReferenceFinderUtil.findByNetexId(deckLevels, deckLevelRefStructure.getRef(), "DeckLevel");
    }

    default DeckLevelRefStructure mapToNetexRef(org.rutebanken.sobek.model.vehicle.DeckLevel level) {
        if(level == null) {
            return null;
        }
        return new DeckLevelRefStructure().withRef(level.getNetexId());
    }

}
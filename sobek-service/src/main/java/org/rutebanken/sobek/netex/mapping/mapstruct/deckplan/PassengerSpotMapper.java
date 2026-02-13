
package org.rutebanken.sobek.netex.mapping.mapstruct.deckplan;

import org.mapstruct.*;
import org.rutebanken.netex.model.*;
import org.rutebanken.sobek.model.vehicle.Deck;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.PointRefStructureMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.PolygonMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.SimplePointMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.equipment.ActualVehicleEquipmentMapper;

import java.util.List;

/**
 * MapStruct mapper for PassengerSpot.
 * Handles mapping between NeTEx PassengerSpot and Sobek PassengerSpot entity.
 */
@Mapper(
        config = SobekMapperConfig.class,
        uses = {
                DataManagedObjectStructureMapper.class,
                PointRefStructureMapper.class,
                SimplePointMapper.class,
                PolygonMapper.class,
                ActualVehicleEquipmentMapper.class
        }
)
public interface PassengerSpotMapper {

    /**
     * Maps from NeTEx PassengerSpot to Sobek entity.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    @Mapping(target = "polygon", source = "polygon", qualifiedByName = "polygonTypeToPolygon")
    org.rutebanken.sobek.model.vehicle.PassengerSpot mapToSobek(
            PassengerSpot source,
            @Context MappingContext context
    );

    /**
     * Maps from Sobek entity back to NeTEx PassengerSpot.
     */
    @DataManagedObjectStructureMapper.ToNetexMappings
    @Mapping(target = "typeOfLocatableSpotRef", ignore = true) // TODO: Implement when needed
//    @Mapping(target = "passengerSeatRef", ignore = true) // TODO: Implement when needed
//    @Mapping(target = "spotAffinities", ignore = true)
    PassengerSpot mapToNetex(
            org.rutebanken.sobek.model.vehicle.PassengerSpot source,
            @Context MappingContext context
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    @Mapping(target = "polygon", source = "polygon", qualifiedByName = "polygonTypeToPolygon")
    @Mapping(target = "actualVehicleEquipments", ignore = true)
    void updateSobekFromNetex(
            PassengerSpot source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.PassengerSpot target,
            @Context MappingContext context
    );

    /**
     * Maps a list of PassengerSpots from Sobek to NeTEx RelStructure.
     */
    default PassengerSpots_RelStructure mapSobekListToNetexRelStructure(
            List<org.rutebanken.sobek.model.vehicle.PassengerSpot> source,
            @Context MappingContext context
    ) {
        if (source == null) {
            return null;
        }

        if (source.isEmpty()) {
            return new PassengerSpots_RelStructure();
        }

        return new PassengerSpots_RelStructure().withPassengerSpotRefOrPassengerSpot(source.stream()
                .map(sobekSpot -> mapToNetex(sobekSpot, context))
                .collect(java.util.stream.Collectors.toList()));
    }

    /**
     * Maps a list of PassengerSpots from NeTEx RelStructure to Sobek.
     */
    default List<org.rutebanken.sobek.model.vehicle.PassengerSpot> mapNetexRelStructureToSobekList(
            PassengerSpots_RelStructure source,
            @Context MappingContext context
    ) {
        if (source == null || source.getPassengerSpotRefOrPassengerSpot() == null) {
            return null;
        }

        return source.getPassengerSpotRefOrPassengerSpot().stream()
                .map(netexSpot -> mapToSobek((PassengerSpot) netexSpot, context))// TODO: Each item could be either PassengerSpot or PassengerSpotRef
                .collect(java.util.stream.Collectors.toList());
    }

    @AfterMapping
    default void afterMapToSobek(
            PassengerSpot source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.PassengerSpot target,
            @Context MappingContext context
    ) {
        Deck currentSobekDeck = context.getCurrentSobekDeck();
        if(source.getSpotColumnRef() != null &&
                source.getSpotColumnRef().getRef() != null) {
            if (currentSobekDeck != null && currentSobekDeck.getSpotColumns() != null) {
                String refId = source.getSpotColumnRef().getRef();
                currentSobekDeck.getSpotColumns().stream()
                        .filter(column -> refId.equals(column.getNetexId()))
                        .findFirst()
                        .ifPresent(target::setSpotColumn);
            }
        }

        if(source.getSpotRowRef() != null &&
                source.getSpotRowRef().getRef() != null) {
            if (currentSobekDeck != null && currentSobekDeck.getSpotRows() != null) {
                String refId = source.getSpotRowRef().getRef();
                currentSobekDeck.getSpotRows().stream()
                        .filter(column -> refId.equals(column.getNetexId()))
                        .findFirst()
                        .ifPresent(target::setSpotRow);
            }
        }
    }

    @AfterMapping
    default void afterMapToNetex(
            org.rutebanken.sobek.model.vehicle.PassengerSpot source,
            @MappingTarget PassengerSpot target,
            @Context MappingContext context
    ) {
        if(source.getSpotColumn() != null) {
            target.setSpotColumnRef(new SpotColumnRefStructure().withRef(source.getSpotColumn().getNetexId()) );
        }

        if(source.getSpotRow() != null) {
            target.setSpotRowRef(new SpotRowRefStructure().withRef(source.getSpotRow().getNetexId()) );
        }
    }
}

package org.rutebanken.sobek.netex.mapping.mapstruct.deckplan;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.rutebanken.netex.model.LuggageSpot;
import org.rutebanken.netex.model.LuggageSpots_RelStructure;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.PointRefStructureMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.PolygonMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.SimplePointMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.equipment.ActualVehicleEquipmentMapper;

import java.util.List;

/**
 * MapStruct mapper for LuggageSpot.
 * Handles mapping between NeTEx LuggageSpot and Sobek LuggageSpot entity.
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
public interface LuggageSpotMapper {

    /**
     * Maps from NeTEx LuggageSpot to Sobek entity.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    @Mapping(target = "polygon", source = "polygon", qualifiedByName = "polygonTypeToPolygon")
    org.rutebanken.sobek.model.vehicle.LuggageSpot mapToSobek(
            LuggageSpot source,
            @Context MappingContext context
    );

    /**
     * Maps from Sobek entity back to NeTEx LuggageSpot.
     */
    @DataManagedObjectStructureMapper.ToNetexMappings
    @Mapping(target = "typeOfLocatableSpotRef", ignore = true) // TODO: Implement when needed
    LuggageSpot mapToNetex(
            org.rutebanken.sobek.model.vehicle.LuggageSpot source,
            @Context MappingContext context
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    @Mapping(target = "polygon", source = "polygon", qualifiedByName = "polygonTypeToPolygon")
    void updateSobekFromNetex(
            LuggageSpot source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.LuggageSpot target,
            @Context MappingContext context
    );

    /**
     * Maps a list of LuggageSpots from Sobek to NeTEx RelStructure.
     */
    default LuggageSpots_RelStructure mapSobekListToNetexRelStructure(
            List<org.rutebanken.sobek.model.vehicle.LuggageSpot> source,
            @Context MappingContext context
    ) {
        if (source == null) {
            return null;
        }

        if (source.isEmpty()) {
            return new LuggageSpots_RelStructure();
        }

        return new LuggageSpots_RelStructure().withLuggageSpotRefOrLuggageSpot(source.stream()
                .map(sobekSpot -> mapToNetex(sobekSpot, context))
                .collect(java.util.stream.Collectors.toList()));
    }

    /**
     * Maps a list of LuggageSpots from NeTEx RelStructure to Sobek.
     */
    default List<org.rutebanken.sobek.model.vehicle.LuggageSpot> mapNetexRelStructureToSobekList(
            LuggageSpots_RelStructure source,
            @Context MappingContext context
    ) {
        if (source == null || source.getLuggageSpotRefOrLuggageSpot() == null) {
            return null;
        }

        return source.getLuggageSpotRefOrLuggageSpot().stream()
                .map(netexSpot -> mapToSobek((LuggageSpot) netexSpot, context))// TODO: Each item could be either LuggageSpot or LuggageSpotRef
                .collect(java.util.stream.Collectors.toList());
    }
}
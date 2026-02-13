package org.rutebanken.sobek.netex.mapping.mapstruct.deckplan;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.rutebanken.netex.model.SpotColumn;
import org.rutebanken.netex.model.SpotColumn_VersionStructure;
import org.rutebanken.netex.model.SpotColumns_RelStructure;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.EntityInVersionMapper;

import java.util.List;

/**
 * MapStruct mapper for SpotColumn.
 * Handles mapping between NeTEx SpotColumn and Sobek SpotColumn entity.
 */
@Mapper(
        config = SobekMapperConfig.class,
        uses = {EntityInVersionMapper.class}
)
public interface SpotColumnMapper {

    /**
     * Maps from NeTEx SpotColumn to Sobek entity.
     */
    @EntityInVersionMapper.ToSobekMappings
    org.rutebanken.sobek.model.vehicle.SpotColumn mapToSobek(
            SpotColumn_VersionStructure source,
            @Context MappingContext context
    );

    /**
     * Maps from Sobek entity back to NeTEx SpotColumn.
     */
    @EntityInVersionMapper.ToNetexMappings
    SpotColumn mapToNetex(
            org.rutebanken.sobek.model.vehicle.SpotColumn source,
            @Context MappingContext context
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    @EntityInVersionMapper.ToSobekMappings
    void updateSobekFromNetex(
            SpotColumn source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.SpotColumn target,
            @Context MappingContext context
    );

    /**
     * Maps a list of SpotColumns from Sobek to NeTEx RelStructure.
     */
    default SpotColumns_RelStructure mapSobekListToNetexRelStructure(
            List<org.rutebanken.sobek.model.vehicle.SpotColumn> source,
            @Context MappingContext context
    ) {
        if (source == null) {
            return null;
        }

        if (source.isEmpty()) {
            return new SpotColumns_RelStructure();
        }

        return new SpotColumns_RelStructure().withSpotColumn(source.stream()
                .map(sobekSpotColumn -> mapToNetex(sobekSpotColumn, context))
                .collect(java.util.stream.Collectors.toList()));
    }

    /**
     * Maps a list of SpotColumns from NeTEx RelStructure to Sobek.
     */
    default List<org.rutebanken.sobek.model.vehicle.SpotColumn> mapNetexRelStructureToSobekList(
            SpotColumns_RelStructure source,
            @Context MappingContext context
    ) {
        if (source == null || source.getSpotColumn() == null) {
            return null;
        }

        return source.getSpotColumn().stream()
                .map(netexSpotColumn -> mapToSobek(netexSpotColumn, context))
                .collect(java.util.stream.Collectors.toList());
    }
}
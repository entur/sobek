package org.rutebanken.sobek.netex.mapping.mapstruct.deckplan;

import org.mapstruct.*;
import org.rutebanken.netex.model.SpotRow;
import org.rutebanken.netex.model.SpotRow_VersionStructure;
import org.rutebanken.netex.model.SpotRows_RelStructure;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.EntityInVersionMapper;

import java.util.List;

/**
 * MapStruct mapper for SpotRow.
 * Handles mapping between NeTEx SpotRow and Sobek SpotRow entity.
 */
@Mapper(
        config = SobekMapperConfig.class,
        uses = {EntityInVersionMapper.class}
)
public interface SpotRowMapper {

    /**
     * Maps from NeTEx SpotRow to Sobek entity.
     */
    @EntityInVersionMapper.ToSobekMappings
    org.rutebanken.sobek.model.vehicle.SpotRow mapToSobek(
            SpotRow_VersionStructure source,
            @Context MappingContext context
    );

    /**
     * Maps from Sobek entity back to NeTEx SpotRow.
     */
    @EntityInVersionMapper.ToNetexMappings
    SpotRow mapToNetex(
            org.rutebanken.sobek.model.vehicle.SpotRow source,
            @Context MappingContext context
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    @EntityInVersionMapper.ToSobekMappings
    void updateSobekFromNetex(
            SpotRow source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.SpotRow target,
            @Context MappingContext context
    );

    /**
     * Maps a list of SpotRows from Sobek to NeTEx RelStructure.
     */
    default SpotRows_RelStructure mapSobekListToNetexRelStructure(
            List<org.rutebanken.sobek.model.vehicle.SpotRow> source,
            @Context MappingContext context
    ) {
        if (source == null) {
            return null;
        }

        if (source.isEmpty()) {
            return new SpotRows_RelStructure();
        }

        return new SpotRows_RelStructure().withSpotRow(source.stream()
                .map(sobekSpotRow -> mapToNetex(sobekSpotRow, context))
                .collect(java.util.stream.Collectors.toList()));
    }

    /**
     * Maps a list of SpotRows from NeTEx RelStructure to Sobek.
     */
    default List<org.rutebanken.sobek.model.vehicle.SpotRow> mapNetexRelStructureToSobekList(
            SpotRows_RelStructure source,
            @Context MappingContext context
    ) {
        if (source == null || source.getSpotRow() == null) {
            return null;
        }

        return source.getSpotRow().stream()
                .map(netexSpotRow -> mapToSobek(netexSpotRow, context))
                .collect(java.util.stream.Collectors.toList());
    }
}
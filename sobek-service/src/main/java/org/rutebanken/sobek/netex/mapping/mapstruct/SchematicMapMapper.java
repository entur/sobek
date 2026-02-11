package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.rutebanken.netex.model.SchematicMap;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;

import java.util.List;

/**
 * MapStruct mapper for SchematicMap.
 * Handles mapping between NeTEx SchematicMap and Sobek SchematicMap entity.
 */
@Mapper(
        config = SobekMapperConfig.class,
        uses = {
                SchematicMapMemberMapper.class,
                VersionOfObjectRefStructureToStringMapper.class
        }
)
public interface SchematicMapMapper {

    /**
     * Maps from NeTEx SchematicMap to Sobek entity.
     */
    @EntityStructureMapper.EntityStructureToSobekMappings
    org.rutebanken.sobek.model.vehicle.SchematicMap mapToSobek(
            SchematicMap source,
            @Context MappingContext context
    );

    /**
     * Maps from Sobek entity back to NeTEx SchematicMap.
     */
    @EntityStructureMapper.EntityStructureToNetexMappings
    SchematicMap mapToNetex(
            org.rutebanken.sobek.model.vehicle.SchematicMap source,
            @Context MappingContext context
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    @EntityStructureMapper.EntityStructureToSobekMappings
    void updateSobekFromNetex(
            SchematicMap source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.SchematicMap target,
            @Context MappingContext context
    );

     /**
     * Maps a list of SchematicMaps.
     */
    List<org.rutebanken.sobek.model.vehicle.SchematicMap> mapAsList(
            List<SchematicMap> sourceList,
            @Context MappingContext context
    );
}
package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.mapstruct.*;
import org.rutebanken.netex.model.SchematicMapMember_VersionedChildStructure;
import org.rutebanken.netex.model.SchematicMapMembers_RelStructure;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;

import java.util.List;

/**
 * MapStruct mapper for SchematicMap.
 * Handles mapping between NeTEx SchematicMap and Sobek SchematicMap entity.
 */
@Mapper(
        config = SobekMapperConfig.class
)
public interface SchematicMapMemberMapper {

    /**
     * Maps from NeTEx SchematicMap to Sobek entity.
     */
    org.rutebanken.sobek.model.vehicle.SchematicMapMember mapToSobek(
            SchematicMapMember_VersionedChildStructure source,
            @Context MappingContext context
    );

    /**
     * Maps from Sobek entity back to NeTEx SchematicMap.
     */
    SchematicMapMember_VersionedChildStructure mapToNetex(
            org.rutebanken.sobek.model.vehicle.SchematicMapMember source,
            @Context MappingContext context
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    void updateSobekFromNetex(
            SchematicMapMember_VersionedChildStructure source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.SchematicMapMember target,
            @Context MappingContext context
    );

    /**
     * Maps a list of SchematicMaps.
     */
    default SchematicMapMembers_RelStructure mapSobekListToNetexRelStructure(
            List<org.rutebanken.sobek.model.vehicle.SchematicMapMember> source,
            @Context MappingContext context
    ) {
        if (source == null) {
            return null;
        }

        if(source.isEmpty()) {
            return new SchematicMapMembers_RelStructure();
        }

        return new SchematicMapMembers_RelStructure().withSchematicMapMember(source.stream()
                .map(sobekMember -> mapToNetex (sobekMember, context))
                .collect(java.util.stream.Collectors.toList()));
    };

    /**
     * Maps a list of SchematicMaps.
     */
    default List<org.rutebanken.sobek.model.vehicle.SchematicMapMember> mapNetexRelStructureToSobekList(
            SchematicMapMembers_RelStructure source,
            @Context MappingContext context
    ) {
        if (source == null || source.getSchematicMapMember() == null) {
            return null;
        }

        return source.getSchematicMapMember().stream()
                .map(netexMember -> mapToSobek (netexMember, context))
                .collect(java.util.stream.Collectors.toList());
    };
}
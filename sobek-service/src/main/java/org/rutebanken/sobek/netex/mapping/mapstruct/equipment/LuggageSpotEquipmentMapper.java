
package org.rutebanken.sobek.netex.mapping.mapstruct.equipment;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.rutebanken.netex.model.LuggageSpotEquipment;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapper;

/**
 * MapStruct mapper for LuggageSpotEquipment.
 * Handles mapping between NeTEx LuggageSpotEquipment and Sobek LuggageSpotEquipment entity.
 */
@Mapper(
        config = SobekMapperConfig.class,
        uses = {
                DataManagedObjectStructureMapper.class
        }
)
public interface LuggageSpotEquipmentMapper {

    /**
     * Maps from NeTEx LuggageSpotEquipment to Sobek entity.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    org.rutebanken.sobek.model.vehicle.LuggageSpotEquipment mapToSobek(
            LuggageSpotEquipment source,
            @Context MappingContext context
    );

    /**
     * Maps from Sobek entity back to NeTEx LuggageSpotEquipment.
     */
    @DataManagedObjectStructureMapper.ToNetexMappings
    LuggageSpotEquipment mapToNetex(
            org.rutebanken.sobek.model.vehicle.LuggageSpotEquipment source,
            @Context MappingContext context
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    void updateSobekFromNetex(
            LuggageSpotEquipment source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.LuggageSpotEquipment target,
            @Context MappingContext context
    );

    @AfterMapping
    default void afterMapToSobek(
            LuggageSpotEquipment source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.LuggageSpotEquipment target,
            @Context MappingContext context
    ) {
        if (target != null) {
            context.getDataManagedObjectStructureMapper().afterMappingToSobek(source, target, context);
        }
    }
    @AfterMapping
    default void afterMapToNetex(
            org.rutebanken.sobek.model.vehicle.LuggageSpotEquipment source,
            @MappingTarget LuggageSpotEquipment target,
            @Context MappingContext context
    ) {
        if (target != null) {
            context.getDataManagedObjectStructureMapper().afterMappingToNetex(source, target, context);
        }
    }
}
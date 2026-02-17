
package org.rutebanken.sobek.netex.mapping.mapstruct.equipment;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.rutebanken.netex.model.SpotEquipment;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapper;

/**
 * MapStruct mapper for SpotEquipment.
 * Handles mapping between NeTEx SpotEquipment and Sobek SpotEquipment entity.
 */
@Mapper(
        config = SobekMapperConfig.class,
        uses = {
                DataManagedObjectStructureMapper.class
        }
)
public interface SpotEquipmentMapper {

    /**
     * Maps from NeTEx SpotEquipment to Sobek entity.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    org.rutebanken.sobek.model.vehicle.SpotEquipment mapToSobek(
            SpotEquipment source,
            @Context MappingContext context
    );

    /**
     * Maps from Sobek entity back to NeTEx SpotEquipment.
     */
    @DataManagedObjectStructureMapper.ToNetexMappings
    SpotEquipment mapToNetex(
            org.rutebanken.sobek.model.vehicle.SpotEquipment source,
            @Context MappingContext context
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    void updateSobekFromNetex(
            SpotEquipment source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.SpotEquipment target,
            @Context MappingContext context
    );

    @AfterMapping
    default void afterMapToSobek(
            SpotEquipment source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.SpotEquipment target,
            @Context MappingContext context
    ) {
        if (target != null) {
            context.getDataManagedObjectStructureMapper().afterMappingToSobek(source, target, context);
        }
    }
    @AfterMapping
    default void afterMapToNetex(
            org.rutebanken.sobek.model.vehicle.SpotEquipment source,
            @MappingTarget SpotEquipment target,
            @Context MappingContext context
    ) {
        if (target != null) {
            context.getDataManagedObjectStructureMapper().afterMappingToNetex(source, target, context);
        }
    }
}

package org.rutebanken.sobek.netex.mapping.mapstruct.equipment;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.rutebanken.netex.model.SeatEquipment;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapper;

/**
 * MapStruct mapper for SeatEquipment.
 * Handles mapping between NeTEx SeatEquipment and Sobek SeatEquipment entity.
 */
@Mapper(
        config = SobekMapperConfig.class,
        uses = {
                DataManagedObjectStructureMapper.class
        }
)
public interface SeatEquipmentMapper {

    /**
     * Maps from NeTEx SeatEquipment to Sobek entity.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    org.rutebanken.sobek.model.vehicle.SeatEquipment mapToSobek(
            SeatEquipment source,
            @Context MappingContext context
    );

    /**
     * Maps from Sobek entity back to NeTEx SeatEquipment.
     */
    @DataManagedObjectStructureMapper.ToNetexMappings
    SeatEquipment mapToNetex(
            org.rutebanken.sobek.model.vehicle.SeatEquipment source,
            @Context MappingContext context
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    void updateSobekFromNetex(
            SeatEquipment source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.SeatEquipment target,
            @Context MappingContext context
    );

    @AfterMapping
    default void afterMapToSobek(
            SeatEquipment source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.SeatEquipment target,
            @Context MappingContext context
    ) {
        if (target != null) {
            context.getDataManagedObjectStructureMapper().afterMappingToSobek(source, target, context);
        }
    }
    @AfterMapping
    default void afterMapToNetex(
            org.rutebanken.sobek.model.vehicle.SeatEquipment source,
            @MappingTarget SeatEquipment target,
            @Context MappingContext context
    ) {
        if (target != null) {
            context.getDataManagedObjectStructureMapper().afterMappingToNetex(source, target, context);
        }
    }
}
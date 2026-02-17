
package org.rutebanken.sobek.netex.mapping.mapstruct.equipment;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.rutebanken.netex.model.StaircaseEquipment;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapper;

/**
 * MapStruct mapper for StaircaseEquipment.
 * Handles mapping between NeTEx StaircaseEquipment and Sobek StaircaseEquipment entity.
 */
@Mapper(
        config = SobekMapperConfig.class,
        uses = {
                DataManagedObjectStructureMapper.class
        }
)
public interface StaircaseEquipmentMapper {

    /**
     * Maps from NeTEx StaircaseEquipment to Sobek entity.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    org.rutebanken.sobek.model.vehicle.StaircaseEquipment mapToSobek(
            StaircaseEquipment source,
            @Context MappingContext context
    );

    /**
     * Maps from Sobek entity back to NeTEx StaircaseEquipment.
     */
    @DataManagedObjectStructureMapper.ToNetexMappings
    StaircaseEquipment mapToNetex(
            org.rutebanken.sobek.model.vehicle.StaircaseEquipment source,
            @Context MappingContext context
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    void updateSobekFromNetex(
            StaircaseEquipment source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.StaircaseEquipment target,
            @Context MappingContext context
    );

    @AfterMapping
    default void afterMapToSobek(
            StaircaseEquipment source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.StaircaseEquipment target,
            @Context MappingContext context
    ) {
        if (target != null) {
            context.getDataManagedObjectStructureMapper().afterMappingToSobek(source, target, context);
        }
    }
    @AfterMapping
    default void afterMapToNetex(
            org.rutebanken.sobek.model.vehicle.StaircaseEquipment source,
            @MappingTarget StaircaseEquipment target,
            @Context MappingContext context
    ) {
        if (target != null) {
            context.getDataManagedObjectStructureMapper().afterMappingToNetex(source, target, context);
        }
    }
}
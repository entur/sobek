
package org.rutebanken.sobek.netex.mapping.mapstruct.equipment;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.rutebanken.netex.model.BedEquipment;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapper;

/**
 * MapStruct mapper for BedEquipment.
 * Handles mapping between NeTEx BedEquipment and Sobek BedEquipment entity.
 */
@Mapper(
        config = SobekMapperConfig.class,
        uses = {
                DataManagedObjectStructureMapper.class
        }
)
public interface BedEquipmentMapper {

    /**
     * Maps from NeTEx BedEquipment to Sobek entity.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    org.rutebanken.sobek.model.vehicle.BedEquipment mapToSobek(
            BedEquipment source,
            @Context MappingContext context
    );

    /**
     * Maps from Sobek entity back to NeTEx BedEquipment.
     */
    @DataManagedObjectStructureMapper.ToNetexMappings
    BedEquipment mapToNetex(
            org.rutebanken.sobek.model.vehicle.BedEquipment source,
            @Context MappingContext context
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    void updateSobekFromNetex(
            BedEquipment source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.BedEquipment target,
            @Context MappingContext context
    );

    @AfterMapping
    default void afterMapToSobek(
            BedEquipment source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.BedEquipment target,
            @Context MappingContext context
    ) {
        if (target != null) {
            context.getDataManagedObjectStructureMapper().afterMappingToSobek(source, target, context);
        }
    }
    @AfterMapping
    default void afterMapToNetex(
            org.rutebanken.sobek.model.vehicle.BedEquipment source,
            @MappingTarget BedEquipment target,
            @Context MappingContext context
    ) {
        if (target != null) {
            context.getDataManagedObjectStructureMapper().afterMappingToNetex(source, target, context);
        }
    }
}
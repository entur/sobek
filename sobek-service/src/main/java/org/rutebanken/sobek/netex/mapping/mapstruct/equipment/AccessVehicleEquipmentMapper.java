
package org.rutebanken.sobek.netex.mapping.mapstruct.equipment;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.rutebanken.netex.model.AccessVehicleEquipment;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapper;

/**
 * MapStruct mapper for AccessVehicleEquipment.
 * Handles mapping between NeTEx AccessVehicleEquipment and Sobek AccessVehicleEquipment entity.
 */
@Mapper(
        config = SobekMapperConfig.class,
        uses = {
                DataManagedObjectStructureMapper.class
        }
)
public interface AccessVehicleEquipmentMapper {

    /**
     * Maps from NeTEx AccessVehicleEquipment to Sobek entity.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    org.rutebanken.sobek.model.vehicle.AccessVehicleEquipment mapToSobek(
            AccessVehicleEquipment source,
            @Context MappingContext context
    );

    /**
     * Maps from Sobek entity back to NeTEx AccessVehicleEquipment.
     */
    @DataManagedObjectStructureMapper.ToNetexMappings
    AccessVehicleEquipment mapToNetex(
            org.rutebanken.sobek.model.vehicle.AccessVehicleEquipment source,
            @Context MappingContext context
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    void updateSobekFromNetex(
            AccessVehicleEquipment source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.AccessVehicleEquipment target,
            @Context MappingContext context
    );

    @AfterMapping
    default void afterMapToSobek(
            AccessVehicleEquipment source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.AccessVehicleEquipment target,
            @Context MappingContext context
    ) {
        if (target != null) {
            context.getDataManagedObjectStructureMapper().afterMappingToSobek(source, target, context);
        }
    }
    @AfterMapping
    default void afterMapToNetex(
            org.rutebanken.sobek.model.vehicle.AccessVehicleEquipment source,
            @MappingTarget AccessVehicleEquipment target,
            @Context MappingContext context
    ) {
        if (target != null) {
            context.getDataManagedObjectStructureMapper().afterMappingToNetex(source, target, context);
        }
    }
}

package org.rutebanken.sobek.netex.mapping.mapstruct.equipment;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.rutebanken.netex.model.SeatEquipment;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
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
            SeatEquipment source
    );

    /**
     * Maps from Sobek entity back to NeTEx SeatEquipment.
     */
    @DataManagedObjectStructureMapper.ToNetexMappings
    SeatEquipment mapToNetex(
            org.rutebanken.sobek.model.vehicle.SeatEquipment source
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    void updateSobekFromNetex(
            SeatEquipment source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.SeatEquipment target
    );
}
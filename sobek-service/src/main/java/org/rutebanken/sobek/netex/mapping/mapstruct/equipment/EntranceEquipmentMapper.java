
package org.rutebanken.sobek.netex.mapping.mapstruct.equipment;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.rutebanken.netex.model.EntranceEquipment;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapper;

/**
 * MapStruct mapper for EntranceEquipment.
 * Handles mapping between NeTEx EntranceEquipment and Sobek EntranceEquipment entity.
 */
@Mapper(
        config = SobekMapperConfig.class,
        uses = {
                DataManagedObjectStructureMapper.class
        }
)
public interface EntranceEquipmentMapper {

    /**
     * Maps from NeTEx EntranceEquipment to Sobek entity.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    org.rutebanken.sobek.model.vehicle.EntranceEquipment mapToSobek(
            EntranceEquipment source
    );

    /**
     * Maps from Sobek entity back to NeTEx EntranceEquipment.
     */
    @DataManagedObjectStructureMapper.ToNetexMappings
    EntranceEquipment mapToNetex(
            org.rutebanken.sobek.model.vehicle.EntranceEquipment source
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    void updateSobekFromNetex(
            EntranceEquipment source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.EntranceEquipment target
    );
}
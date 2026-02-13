
package org.rutebanken.sobek.netex.mapping.mapstruct.equipment;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.rutebanken.netex.model.SpotEquipment;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
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
            SpotEquipment source
    );

    /**
     * Maps from Sobek entity back to NeTEx SpotEquipment.
     */
    @DataManagedObjectStructureMapper.ToNetexMappings
    SpotEquipment mapToNetex(
            org.rutebanken.sobek.model.vehicle.SpotEquipment source
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    void updateSobekFromNetex(
            SpotEquipment source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.SpotEquipment target
    );
}
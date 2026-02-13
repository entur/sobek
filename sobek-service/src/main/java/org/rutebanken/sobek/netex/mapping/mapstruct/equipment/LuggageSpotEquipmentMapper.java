
package org.rutebanken.sobek.netex.mapping.mapstruct.equipment;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.rutebanken.netex.model.LuggageSpotEquipment;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
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
            LuggageSpotEquipment source
    );

    /**
     * Maps from Sobek entity back to NeTEx LuggageSpotEquipment.
     */
    @DataManagedObjectStructureMapper.ToNetexMappings
    LuggageSpotEquipment mapToNetex(
            org.rutebanken.sobek.model.vehicle.LuggageSpotEquipment source
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    void updateSobekFromNetex(
            LuggageSpotEquipment source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.LuggageSpotEquipment target
    );
}
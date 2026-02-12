package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.mapstruct.*;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;

/**
 * MapStruct mapper for SchematicMap.
 * Handles mapping between NeTEx SchematicMap and Sobek SchematicMap entity.
 */
@Mapper(
        config = SobekMapperConfig.class,
        uses = DataManagedObjectStructureMapper.class
)
public interface PassengerCapacityMapper {
    /**
     * Maps from NeTEx to Sobek entity.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    org.rutebanken.sobek.model.vehicle.PassengerCapacity mapPassengerCapacityToSobek(
            org.rutebanken.netex.model.PassengerCapacityStructure source,
            @Context MappingContext context
    );

    /**
     * Maps from Sobek entity back to NeTEx SchematicMap.
     */
    @DataManagedObjectStructureMapper.ToNetexMappings
    org.rutebanken.netex.model.PassengerCapacityStructure mapPassengerCapacityToNetex(
            org.rutebanken.sobek.model.vehicle.PassengerCapacity source,
            @Context MappingContext context
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    void updateSobekFromNetex(
            org.rutebanken.netex.model.PassengerCapacityStructure source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.PassengerCapacity target,
            @Context MappingContext context
    );
}
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
        uses = { DataManagedObjectStructureMapper.class, EntityInVersionMapper.class}
)
public interface PassengerCapacityMapper {
    /**
     * Maps from NeTEx to Sobek entity.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    org.rutebanken.sobek.model.vehicle.PassengerCapacity mapPassengerCapacityToSobek(
            org.entur.netex2.model.PassengerCapacityStructure source,
            @Context MappingContext context
    );

    /**
     * Maps from Sobek entity back to NeTEx SchematicMap.
     */
    @DataManagedObjectStructureMapper.ToNetexMappings
    org.entur.netex2.model.PassengerCapacityStructure mapPassengerCapacityToNetex(
            org.rutebanken.sobek.model.vehicle.PassengerCapacity source,
            @Context MappingContext context
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    void updateSobekFromNetex(
            org.entur.netex2.model.PassengerCapacityStructure source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.PassengerCapacity target,
            @Context MappingContext context
    );
}
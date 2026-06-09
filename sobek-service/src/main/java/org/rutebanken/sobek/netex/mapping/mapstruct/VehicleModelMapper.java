package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.rutebanken.netex.model.ObjectFactory;
import org.rutebanken.netex.model.TransportTypeRefStructure;
import org.rutebanken.netex.model.VehicleModel;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;

import java.util.List;

/**
 * MapStruct mapper for VehicleModel.
 * Replaces the Orika-based VehicleModelMapper.
 */
@Mapper(
        config = SobekMapperConfig.class,
        uses = DataManagedObjectStructureMapper.class
)
public interface VehicleModelMapper {

    ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    /**
     * Maps from NeTEx VehicleModel to Sobek entity.
     * Map A to B in Orika terms.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    @Mapping(target = "equipmentProfiles", ignore = true) // Transient field
    @Mapping(target = "vehicleModelProfileRef", ignore = true) // Transient field
    org.rutebanken.sobek.model.vehicle.VehicleModel mapToSobek(
            VehicleModel source,
            @Context MappingContext context
    );

    /**
     * Maps from Sobek entity back to NeTEx VehicleModel.
     * Map B to A in Orika terms.
     */
    @DataManagedObjectStructureMapper.ToNetexMappings
    @Mapping(target = "transportTypeRef", ignore = true) // Handled in @AfterMapping
    @Mapping(target = "equipmentProfiles", ignore = true)
    @Mapping(target = "vehicleModelProfileRef", ignore = true)
    VehicleModel mapToNetex(
            org.rutebanken.sobek.model.vehicle.VehicleModel source,
            @Context MappingContext context
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    @Mapping(target = "equipmentProfiles", ignore = true)
    @Mapping(target = "vehicleModelProfileRef", ignore = true)
    void updateSobekFromNetex(
            VehicleModel source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.VehicleModel target,
            @Context MappingContext context
    );

    /**
     * After mapping from NeTEx to Sobek: extract transportTypeRef from JAXBElement.
     */
    @AfterMapping
    default void afterMapToSobek(
            VehicleModel source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.VehicleModel target,
            @Context MappingContext context
    ) {
        // Extract and resolve TransportTypeRef if present
        if (source.getTransportTypeRef() != null && source.getTransportTypeRef().getValue() != null) {
            TransportTypeRefStructure transportTypeRefStructure = source.getTransportTypeRef().getValue();

            // Resolve the actual DeckPlan entity if resolver is available
            org.rutebanken.sobek.model.vehicle.VehicleType vehicleType = ReferenceMapper.resolveReference(
                    transportTypeRefStructure,
                    org.rutebanken.sobek.model.vehicle.VehicleType.class,
                    context
            );
            if (vehicleType != null) {
                target.setTransportType(vehicleType);
            }
        }
    }

    /**
     * After mapping from Sobek to NeTEx: wrap transportTypeRef in JAXBElement.
     * This implements the logic from the original mapBtoA method.
     */
    @AfterMapping
    default void afterMapToNetex(
            org.rutebanken.sobek.model.vehicle.VehicleModel source,
            @MappingTarget VehicleModel target,
            @Context MappingContext context
    ) {
        // Handle TransportType -> TransportTypeRef wrapping
        if (source.getTransportType() != null && source.getTransportType().getNetexId() != null) {
            // Create reference from actual entity
            TransportTypeRefStructure transportTypeRefStructure = ReferenceMapper.createReference(
                    source.getTransportType(),
                    TransportTypeRefStructure.class
            );
            target.withTransportTypeRef(OBJECT_FACTORY.createTransportTypeRef(transportTypeRefStructure));
        }
    }

    List<org.rutebanken.sobek.model.vehicle.VehicleModel> mapAsList(List<VehicleModel> neTExList, @Context MappingContext context);
}
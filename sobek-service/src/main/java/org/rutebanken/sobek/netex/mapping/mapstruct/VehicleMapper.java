package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.entur.netex2.model.*;
import org.rutebanken.sobek.netex.id.VehicleModelId;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;

import java.util.List;

/**
 * MapStruct mapper for Vehicle.
 * Handles mapping between NeTEx Vehicle and Sobek Vehicle entity.
 * A vehicle references its VehicleType (transportType). The vehicle model is the one of the type, so the vehicle has
 * no reference to a model of its own in Sobek: one on an incoming vehicle is checked by the import handler.
 */
@Mapper(
        config = SobekMapperConfig.class,
        uses = {
                DataManagedObjectStructureMapper.class,
                VehicleTypeMapper.class,
                EntityInVersionMapper.class
        }
)
public interface VehicleMapper {

    ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    /**
     * Maps from NeTEx Vehicle to Sobek entity.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    @Mapping(target = "transportType", ignore = true) // Handled in @AfterMapping
    @Mapping(target = "transportTypeRef", ignore = true) // Transient field
    //@Mapping(target = "vehicleTypeRef", ignore = true) // Transient field
    @Mapping(target = "actualVehicleEquipments", ignore = true) // Transient field
    @Mapping(target = "equipmentProfiles", ignore = true) // Transient field
    //@Mapping(target = "transportOrganisationRef", ignore = true) // Transient field
    //@Mapping(target = "brandingRef", ignore = true) // Transient field
    //@Mapping(target = "contactRef", ignore = true) // Transient field
    org.rutebanken.sobek.model.vehicle.Vehicle mapToSobek(
            Vehicle source,
            @Context MappingContext context
    );

    /**
     * Maps from Sobek entity back to NeTEx Vehicle.
     */
    @DataManagedObjectStructureMapper.ToNetexMappings
    @Mapping(target = "transportTypeRef", ignore = true) // Handled in @AfterMapping
    @Mapping(target = "actualVehicleEquipments", ignore = true)
    @Mapping(target = "equipmentProfiles", ignore = true)
    @Mapping(target = "transportOrganisationRef", ignore = true)
    @Mapping(target = "brandingRef", ignore = true)
    @Mapping(target = "contactRef", ignore = true)
    Vehicle mapToNetex(
            org.rutebanken.sobek.model.vehicle.Vehicle source,
            @Context MappingContext context
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    @Mapping(target = "transportType", ignore = true)
    @Mapping(target = "transportTypeRef", ignore = true)
    @Mapping(target = "actualVehicleEquipments", ignore = true)
    @Mapping(target = "equipmentProfiles", ignore = true)
    //@Mapping(target = "transportOrganisationRef", ignore = true)
    //@Mapping(target = "brandingRef", ignore = true)
    //@Mapping(target = "contactRef", ignore = true)
    void updateSobekFromNetex(
            Vehicle source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.Vehicle target,
            @Context MappingContext context
    );

    /**
     * After mapping from NeTEx to Sobek: handle reference resolution.
     * This implements the logic from the original VehicleMapper.mapAtoB method.
     */
    @AfterMapping
    default void afterMapToSobek(
            Vehicle source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.Vehicle target,
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

        context.getOwnedEntityMapper().updateSobekFromNetex(target, context);
    }

    /**
     * After mapping from Sobek to NeTEx: handle reference wrapping.
     * This implements the logic from the original VehicleMapper.mapBtoA method.
     */
    @AfterMapping
    default void afterMapToNetex(
            org.rutebanken.sobek.model.vehicle.Vehicle source,
            @MappingTarget Vehicle target,
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

            // The vehicle model of a vehicle is the one of its type, exported with the id derived from the id of the type
            if (source.getTransportType().hasVehicleModelData()) {
                target.withVehicleModelRef(new VehicleModelRefStructure()
                        .withRef(VehicleModelId.forVehicleType(source.getTransportType().getNetexId()))
                        .withVersion(String.valueOf(source.getTransportType().getVersion())));
            }
        }
    }

    List<org.rutebanken.sobek.model.vehicle.Vehicle> mapAsList(List<Vehicle> sourceList, @Context MappingContext context);
}
package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.mapstruct.*;
import org.rutebanken.netex.model.DeckPlanRefStructure;
import org.rutebanken.netex.model.ObjectFactory;
import org.rutebanken.netex.model.VehicleType;
import org.rutebanken.sobek.model.vehicle.DeckPlan;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;


/**
 * MapStruct mapper for VehicleType.
 * Handles mapping between NeTEx VehicleType and Sobek VehicleType entity.
 * VehicleType extends TransportType_VersionStructure which extends DataManagedObjectStructure.
 */
@Mapper(
        config = SobekMapperConfig.class,
        uses = { PassengerCapacityMapper.class,
                DataManagedObjectStructureMapper.class
        }
)
public interface VehicleTypeMapper {

    ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    /**
     * Maps from NeTEx VehicleType to Sobek entity.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    @Mapping(target = "vehicles", ignore = true) // Bidirectional relationship managed separately
    @Mapping(target = "includedIn", ignore = true) // Transient field
    @Mapping(target = "classifiedAsRef", ignore = true) // Transient field
    @Mapping(target = "facilities", ignore = true) // Transient field
    @Mapping(target = "canCarry", ignore = true) // Transient field
    @Mapping(target = "canManoeuvre", ignore = true) // Transient field
    @Mapping(target = "satisfiesFacilityRequirements", ignore = true) // Transient field
    @Mapping(target = "deckPlanRef", ignore = true) // Handled in @AfterMapping
    org.rutebanken.sobek.model.vehicle.VehicleType mapToSobek(
            VehicleType source,
            @Context MappingContext context
    );

    /**
     * Maps from Sobek entity back to NeTEx VehicleType.
     */
    @DataManagedObjectStructureMapper.ToNetexMappings
    @Mapping(target = "deckPlanRef", ignore = true) // Handled in @AfterMapping
    @Mapping(target = "includedIn", ignore = true)
    @Mapping(target = "classifiedAsRef", ignore = true)
    @Mapping(target = "facilities", ignore = true)
    @Mapping(target = "canCarry", ignore = true)
    @Mapping(target = "canManoeuvre", ignore = true)
    @Mapping(target = "satisfiesFacilityRequirements", ignore = true)
    VehicleType mapToNetex(
            org.rutebanken.sobek.model.vehicle.VehicleType source,
            @Context MappingContext context
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    @Mapping(target = "vehicles", ignore = true)
    @Mapping(target = "includedIn", ignore = true)
    @Mapping(target = "classifiedAsRef", ignore = true)
    @Mapping(target = "facilities", ignore = true)
    @Mapping(target = "canCarry", ignore = true)
    @Mapping(target = "canManoeuvre", ignore = true)
    @Mapping(target = "satisfiesFacilityRequirements", ignore = true)
    @Mapping(target = "deckPlanRef", ignore = true) // Handled in @AfterMapping
    void updateSobekFromNetex(
            VehicleType source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.VehicleType target,
            @Context MappingContext context
    );

    /**
     * After mapping from NeTEx to Sobek: handle DeckPlanRef extraction.
     */
    @AfterMapping
    default void afterMapToSobek(
            VehicleType source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.VehicleType target,
            @Context MappingContext context
    ) {
        if (target != null) {
            context.getDataManagedObjectStructureMapper().afterMappingToSobek(source, target, context);
        }

        // Extract and resolve DeckPlanRef if present
        if (source.getDeckPlanRef() != null) {
            DeckPlanRefStructure deckPlanRef = source.getDeckPlanRef();

            // Resolve the actual DeckPlan entity if resolver is available
            DeckPlan deckPlan = ReferenceMapper.resolveReference(
                    deckPlanRef,
                    DeckPlan.class,
                    context
            );
            if (deckPlan != null) {
                target.setDeckPlan(deckPlan);
            }
        }
    }

    /**
     * After mapping from Sobek to NeTEx: handle DeckPlanRef wrapping.
     */
    @AfterMapping
    default void afterMapToNetex(
            org.rutebanken.sobek.model.vehicle.VehicleType source,
            @MappingTarget VehicleType target,
            @Context MappingContext context
    ) {
        if (target != null) {
            context.getDataManagedObjectStructureMapper().afterMappingToNetex(source, target, context);
        }

        // Handle DeckPlanRef creation from entity or transient reference
        if (source.getDeckPlan() != null && source.getDeckPlan().getNetexId() != null) {
            // Create reference from actual entity
            DeckPlanRefStructure deckPlanRef = ReferenceMapper.createReference(
                    source.getDeckPlan(),
                    DeckPlanRefStructure.class
            );
            target.withDeckPlanRef(deckPlanRef);
        }
    }}

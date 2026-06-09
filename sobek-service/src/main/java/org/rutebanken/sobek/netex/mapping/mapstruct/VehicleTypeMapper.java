package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.mapstruct.*;
import org.rutebanken.netex.model.DeckPlanRefStructure;
import org.rutebanken.netex.model.ObjectFactory;
import org.rutebanken.netex.model.VehicleType;
import org.rutebanken.sobek.model.vehicle.DeckPlan;
import org.rutebanken.sobek.model.vehicle.HybridCategoryEnumeration;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.util.KeyValuesHelper;

import java.math.BigDecimal;


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
            KeyValuesHelper.SetFromKeyValues("FormDragCoefficient", source.getKeyList(), BigDecimal::new, target::setFormDragCoefficient);
            KeyValuesHelper.SetFromKeyValues("RollResistanceCoefficient", source.getKeyList(), BigDecimal::new, target::setRollResistanceCoefficient);
            KeyValuesHelper.SetFromKeyValues("MaximumEngineEffectKW", source.getKeyList(), BigDecimal::new, target::setMaximumEngineEffectKW);
            KeyValuesHelper.SetFromKeyValues("HybridCategory", source.getKeyList(), HybridCategoryEnumeration::fromValue, target::setHybridCategory);
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
        context.getOwnedEntityMapper().updateSobekFromNetex(target, context);
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

        if(target != null) {
            KeyValuesHelper.AddToKeyValues(target, "FormDragCoefficient", source.getFormDragCoefficient() == null ? null : String.valueOf(source.getFormDragCoefficient()));
            KeyValuesHelper.AddToKeyValues(target, "RollResistanceCoefficient", source.getRollResistanceCoefficient() == null ? null : String.valueOf(source.getRollResistanceCoefficient()));
            KeyValuesHelper.AddToKeyValues(target, "MaximumEngineEffectKW", source.getMaximumEngineEffectKW() == null ? null : String.valueOf(source.getMaximumEngineEffectKW()));
            KeyValuesHelper.AddToKeyValues(target, "HybridCategory", source.getHybridCategory() == null ? null : String.valueOf(source.getHybridCategory()));
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

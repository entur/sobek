package org.rutebanken.sobek.netex.mapping.mapstruct;

import jakarta.xml.bind.JAXBElement;
import org.hibernate.Hibernate;
import org.mapstruct.*;
import org.entur.netex2.model.DataManagedObjectStructure;
import org.entur.netex2.model.DeckPlanRefStructure;
import org.entur.netex2.model.ObjectFactory;
import org.entur.netex2.model.VehicleType;
import org.entur.netex2.model.VehicleType_VersionStructure;
import org.rutebanken.sobek.model.vehicle.DeckPlan;
import org.rutebanken.sobek.model.vehicle.HybridCategoryEnumeration;
import org.rutebanken.sobek.model.vehicle.VehicleTypeKindEnumeration;
import org.rutebanken.sobek.netex.id.ValidPrefixList;
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
                DataManagedObjectStructureMapper.class,
                EntityInVersionMapper.class
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
     * NeTEx Train and CompoundTrain are not subtypes of the NeTEx VehicleType, so they get methods of their own. The
     * properties of the vehicle type are mapped as for VehicleType; the size and the components are added after.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    @Mapping(target = "vehicles", ignore = true)
    @Mapping(target = "includedIn", ignore = true)
    @Mapping(target = "classifiedAsRef", ignore = true)
    @Mapping(target = "facilities", ignore = true)
    @Mapping(target = "canCarry", ignore = true)
    @Mapping(target = "canManoeuvre", ignore = true)
    @Mapping(target = "satisfiesFacilityRequirements", ignore = true)
    @Mapping(target = "deckPlanRef", ignore = true)
    @Mapping(target = "components", ignore = true) // Handled in @AfterMapping
    @Mapping(target = "numberOfCars", ignore = true)
    @Mapping(target = "trainSizeType", ignore = true)
    org.rutebanken.sobek.model.vehicle.Train mapToSobek(
            org.entur.netex2.model.Train source,
            @Context MappingContext context
    );

    @DataManagedObjectStructureMapper.ToSobekMappings
    @Mapping(target = "vehicles", ignore = true)
    @Mapping(target = "includedIn", ignore = true)
    @Mapping(target = "classifiedAsRef", ignore = true)
    @Mapping(target = "facilities", ignore = true)
    @Mapping(target = "canCarry", ignore = true)
    @Mapping(target = "canManoeuvre", ignore = true)
    @Mapping(target = "satisfiesFacilityRequirements", ignore = true)
    @Mapping(target = "deckPlanRef", ignore = true)
    @Mapping(target = "components", ignore = true) // Handled in @AfterMapping
    org.rutebanken.sobek.model.vehicle.CompoundTrain mapToSobek(
            org.entur.netex2.model.CompoundTrain source,
            @Context MappingContext context
    );

    @DataManagedObjectStructureMapper.ToNetexMappings
    @Mapping(target = "deckPlanRef", ignore = true)
    @Mapping(target = "includedIn", ignore = true)
    @Mapping(target = "classifiedAsRef", ignore = true)
    @Mapping(target = "facilities", ignore = true)
    @Mapping(target = "canCarry", ignore = true)
    @Mapping(target = "canManoeuvre", ignore = true)
    @Mapping(target = "satisfiesFacilityRequirements", ignore = true)
    @Mapping(target = "trainSize", ignore = true) // Handled in @AfterMapping
    @Mapping(target = "components", ignore = true) // Handled in @AfterMapping
    org.entur.netex2.model.Train mapToNetex(
            org.rutebanken.sobek.model.vehicle.Train source,
            @Context MappingContext context
    );

    @DataManagedObjectStructureMapper.ToNetexMappings
    @Mapping(target = "deckPlanRef", ignore = true)
    @Mapping(target = "includedIn", ignore = true)
    @Mapping(target = "classifiedAsRef", ignore = true)
    @Mapping(target = "facilities", ignore = true)
    @Mapping(target = "canCarry", ignore = true)
    @Mapping(target = "canManoeuvre", ignore = true)
    @Mapping(target = "satisfiesFacilityRequirements", ignore = true)
    @Mapping(target = "components", ignore = true) // Handled in @AfterMapping
    org.entur.netex2.model.CompoundTrain mapToNetex(
            org.rutebanken.sobek.model.vehicle.CompoundTrain source,
            @Context MappingContext context
    );

    /**
     * NeTEx TrainElement is a TrainElementType, not a VehicleType, but its size, capacity and equipment fields are the
     * ones of a vehicle type. Only the kind of element (trainElementType) is of its own.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    @Mapping(target = "facilities", ignore = true)
    @Mapping(target = "deckPlanRef", ignore = true) // Handled in @AfterMapping
    org.rutebanken.sobek.model.vehicle.TrainElement mapToSobek(
            org.entur.netex2.model.TrainElement source,
            @Context MappingContext context
    );

    @DataManagedObjectStructureMapper.ToNetexMappings
    @Mapping(target = "facilities", ignore = true)
    @Mapping(target = "deckPlanRef", ignore = true) // Handled in @AfterMapping
    org.entur.netex2.model.TrainElement mapToNetex(
            org.rutebanken.sobek.model.vehicle.TrainElement source,
            @Context MappingContext context
    );

    @AfterMapping
    default void afterMapTrainElementToSobek(org.entur.netex2.model.TrainElement source,
                                             @MappingTarget org.rutebanken.sobek.model.vehicle.TrainElement target,
                                             @Context MappingContext context) {
        afterMapVehicleTypeToSobek(source, source.getDeckPlanRef(), target, context);
    }

    @AfterMapping
    default void afterMapTrainElementToNetex(org.rutebanken.sobek.model.vehicle.TrainElement source,
                                             @MappingTarget org.entur.netex2.model.TrainElement target,
                                             @Context MappingContext context) {
        afterMapVehicleTypeToNetex(source, target, context);
    }

    @AfterMapping
    default void afterMapTrainToSobek(org.entur.netex2.model.Train source,
                                      @MappingTarget org.rutebanken.sobek.model.vehicle.Train target,
                                      @Context MappingContext context) {
        TrainStructureMapping.toSobek(source, target, context);
    }

    @AfterMapping
    default void afterMapCompoundTrainToSobek(org.entur.netex2.model.CompoundTrain source,
                                              @MappingTarget org.rutebanken.sobek.model.vehicle.CompoundTrain target,
                                              @Context MappingContext context) {
        TrainStructureMapping.toSobek(source, target, context);
    }

    @AfterMapping
    default void afterMapTrainToNetex(org.rutebanken.sobek.model.vehicle.Train source,
                                      @MappingTarget org.entur.netex2.model.Train target) {
        TrainStructureMapping.toNetex(source, target);
    }

    @AfterMapping
    default void afterMapCompoundTrainToNetex(org.rutebanken.sobek.model.vehicle.CompoundTrain source,
                                              @MappingTarget org.entur.netex2.model.CompoundTrain target) {
        TrainStructureMapping.toNetex(source, target);
    }

    /**
     * Maps an incoming vehicle type, train or compound train to the Sobek entity of the same kind. (A train element is
     * not a NeTEx vehicle type: it is mapped with {@link #mapToSobek(org.entur.netex2.model.TrainElement, MappingContext)}.)
     */
    default org.rutebanken.sobek.model.vehicle.VehicleType mapAnyToSobek(VehicleType_VersionStructure source, MappingContext context) {
        return switch (source) {
            case org.entur.netex2.model.Train train -> mapToSobek(train, context);
            case org.entur.netex2.model.CompoundTrain compoundTrain -> mapToSobek(compoundTrain, context);
            case VehicleType vehicleType -> mapToSobek(vehicleType, context);
            default -> throw new IllegalArgumentException("Not a vehicle type: " + source.getClass().getSimpleName());
        };
    }

    /**
     * Maps a Sobek vehicle type, train or compound train to the NeTEx element of the same kind. (A train element is not
     * a NeTEx vehicle type: it is mapped with {@link #mapToNetex(org.rutebanken.sobek.model.vehicle.TrainElement, MappingContext)}.)
     */
    default VehicleType_VersionStructure mapAnyToNetex(org.rutebanken.sobek.model.vehicle.VehicleType source, MappingContext context) {
        return switch (Hibernate.unproxy(source)) {
            case org.rutebanken.sobek.model.vehicle.Train train -> mapToNetex(train, context);
            case org.rutebanken.sobek.model.vehicle.CompoundTrain compoundTrain -> mapToNetex(compoundTrain, context);
            case org.rutebanken.sobek.model.vehicle.TrainElement ignored -> throw new IllegalArgumentException(
                    "A train element is not a vehicle type in NeTEx: map it with mapToNetex");
            case org.rutebanken.sobek.model.vehicle.VehicleType vehicleType -> mapToNetex(vehicleType, context);
            default -> throw new IllegalArgumentException("Not a vehicle type: " + source.getClass().getSimpleName());
        };
    }

    /**
     * The JAXB element for a mapped vehicle type, train or compound train, for the transport types of a frame. (Train
     * elements are not transport types: they go in the train element types of the frame, as they are.)
     */
    static JAXBElement<? extends DataManagedObjectStructure> toJaxbElement(VehicleType_VersionStructure netex) {
        return switch (netex) {
            case org.entur.netex2.model.Train train -> OBJECT_FACTORY.createTrain(train);
            case org.entur.netex2.model.CompoundTrain compoundTrain -> OBJECT_FACTORY.createCompoundTrain(compoundTrain);
            case VehicleType vehicleType -> OBJECT_FACTORY.createVehicleType(vehicleType);
            default -> throw new IllegalArgumentException("Not a vehicle type: " + netex.getClass().getSimpleName());
        };
    }

    /**
     * The kind of an incoming vehicle type follows from the codespace of its id.
     */
    static VehicleTypeKindEnumeration kindFromNetexId(String netexId) {
        return netexId != null && ValidPrefixList.isContractualVehicleTypeId(netexId)
                ? VehicleTypeKindEnumeration.CONTRACTUAL
                : VehicleTypeKindEnumeration.PHYSICAL;
    }

    /**
     * After mapping from NeTEx to Sobek: handle DeckPlanRef extraction.
     */
    @AfterMapping
    default void afterMapToSobek(
            VehicleType_VersionStructure source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.VehicleType target,
            @Context MappingContext context
    ) {
        afterMapVehicleTypeToSobek(source, source.getDeckPlanRef(), target, context);
    }

    /**
     * What is done after mapping to a Sobek vehicle type or one of its subtypes, whether the NeTEx element is a
     * vehicle type or a train element.
     */
    default void afterMapVehicleTypeToSobek(
            DataManagedObjectStructure source,
            DeckPlanRefStructure deckPlanRef,
            org.rutebanken.sobek.model.vehicle.VehicleType target,
            MappingContext context
    ) {
        if (target != null) {
            context.getDataManagedObjectStructureMapper().afterMappingToSobek(source, target, context);
            KeyValuesHelper.SetFromKeyValues("FormDragCoefficient", source.getKeyList(), BigDecimal::new, target::setFormDragCoefficient);
            KeyValuesHelper.SetFromKeyValues("RollResistanceCoefficient", source.getKeyList(), BigDecimal::new, target::setRollResistanceCoefficient);
            KeyValuesHelper.SetFromKeyValues("MaximumEngineEffectKW", source.getKeyList(), BigDecimal::new, target::setMaximumEngineEffectKW);
            KeyValuesHelper.SetFromKeyValues("HybridCategory", source.getKeyList(), HybridCategoryEnumeration::fromValue, target::setHybridCategory);
            // NeTEx has no field for the kind (and a KeyValue would break the schema's uniqueness constraint on
            // key/value pairs), so it is expressed by the codespace of the id: CVT is contractual, anything else physical.
            target.setVehicleTypeKind(kindFromNetexId(source.getId()));
        }

        // Extract and resolve DeckPlanRef if present
        if (deckPlanRef != null) {

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
            @MappingTarget VehicleType_VersionStructure target,
            @Context MappingContext context
    ) {
        afterMapVehicleTypeToNetex(source, target, context);
    }

    /**
     * What is done after mapping to NeTEx from a Sobek vehicle type or one of its subtypes, whether the NeTEx element
     * is a vehicle type or a train element.
     */
    default void afterMapVehicleTypeToNetex(
            org.rutebanken.sobek.model.vehicle.VehicleType source,
            DataManagedObjectStructure target,
            MappingContext context
    ) {
        if(target != null) {
            KeyValuesHelper.SetToKeyValues(target, "FormDragCoefficient", source.getFormDragCoefficient() == null ? null : String.valueOf(source.getFormDragCoefficient()));
            KeyValuesHelper.SetToKeyValues(target, "RollResistanceCoefficient", source.getRollResistanceCoefficient() == null ? null : String.valueOf(source.getRollResistanceCoefficient()));
            KeyValuesHelper.SetToKeyValues(target, "MaximumEngineEffectKW", source.getMaximumEngineEffectKW() == null ? null : String.valueOf(source.getMaximumEngineEffectKW()));
            KeyValuesHelper.SetToKeyValues(target, "HybridCategory", source.getHybridCategory() == null ? null : source.getHybridCategory().value());
        }

        // Handle DeckPlanRef creation from entity or transient reference
        if (source.getDeckPlan() != null && source.getDeckPlan().getNetexId() != null) {
            // Create reference from actual entity
            DeckPlanRefStructure deckPlanRef = ReferenceMapper.createReference(
                    source.getDeckPlan(),
                    DeckPlanRefStructure.class
            );
            if (target instanceof VehicleType_VersionStructure vehicleType) {
                vehicleType.withDeckPlanRef(deckPlanRef);
            } else if (target instanceof org.entur.netex2.model.TrainElement trainElement) {
                trainElement.withDeckPlanRef(deckPlanRef);
            }
        }
    }}

package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.entur.netex2.model.CompoundTrain;
import org.entur.netex2.model.ObjectFactory;
import org.entur.netex2.model.Train;
import org.entur.netex2.model.TrainComponent;
import org.entur.netex2.model.TrainComponentCouplingStructure;
import org.entur.netex2.model.TrainComponents_RelStructure;
import org.entur.netex2.model.TrainElementTypeRefStructure;
import org.entur.netex2.model.TrainInCompoundTrain_VersionedChildStructure;
import org.entur.netex2.model.TrainRefStructure;
import org.entur.netex2.model.TrainSizeStructure;
import org.entur.netex2.model.TrainsInCompoundTrain_RelStructure;
import org.entur.netex2.model.VersionOfObjectRefStructure;
import org.rutebanken.sobek.model.vehicle.ThroughAccessEnumeration;
import org.rutebanken.sobek.model.vehicle.TrainInCompoundTrain;
import org.rutebanken.sobek.model.vehicle.VehicleOrientationEnumeration;
import org.rutebanken.sobek.netex.id.CodespaceType;
import org.rutebanken.sobek.netex.mapping.NetexMappingException;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * The parts of Train and CompoundTrain that MapStruct does not map: the size, and the ordered list of components with
 * the references they hold.
 * <p>
 * Components are the children of their train and have no life of their own. An id sent in is kept when it is in Sobek's
 * or a registered codespace, as for any other entity; otherwise (or without an id) the saver assigns one. The version
 * sent in is ignored. The position in the list is the NeTEx {@code order}. A component of a Train refers to a
 * TrainElement in Sobek, and a component of a CompoundTrain to a Train in Sobek. A target that cannot be found is an
 * error: the component would otherwise be saved without it.
 */
public final class TrainStructureMapping {

    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    private static final MultilingualStringMapper STRINGS = new MultilingualStringMapper() {
    };

    private TrainStructureMapping() {
    }

    // ----- NeTEx to Sobek -----

    public static void toSobek(Train source, org.rutebanken.sobek.model.vehicle.Train target, MappingContext context) {
        TrainSizeStructure size = source.getTrainSize();
        target.setNumberOfCars(size == null || size.getNumberOfCars() == null ? null : size.getNumberOfCars().intValueExact());
        target.setTrainSizeType(size == null || size.getTrainSizeType() == null ? null
                : org.rutebanken.sobek.model.vehicle.TrainSizeEnumeration.valueOf(size.getTrainSizeType().name()));

        List<TrainComponent> components = new ArrayList<>();
        if (source.getComponents() != null) {
            for (Object component : source.getComponents().getTrainComponentRefOrTrainComponent()) {
                if (component instanceof TrainComponent trainComponent) {
                    components.add(trainComponent);
                } else {
                    throw new NetexMappingException("Train " + source.getId()
                            + " has a component given by reference. Give the TrainComponent itself.");
                }
            }
        }
        List<org.rutebanken.sobek.model.vehicle.TrainComponent> mapped = new ArrayList<>();
        for (TrainComponent component : inOrder(components, TrainComponent::getOrder)) {
            mapped.add(componentToSobek(component, source.getId(), context));
        }
        target.setComponents(mapped);
    }

    public static void toSobek(CompoundTrain source, org.rutebanken.sobek.model.vehicle.CompoundTrain target, MappingContext context) {
        List<TrainInCompoundTrain_VersionedChildStructure> components = source.getComponents() == null
                ? List.of() : source.getComponents().getTrainInCompoundTrain();
        List<TrainInCompoundTrain> mapped = new ArrayList<>();
        for (TrainInCompoundTrain_VersionedChildStructure component : inOrder(components, TrainInCompoundTrain_VersionedChildStructure::getOrder)) {
            mapped.add(componentToSobek(component, source.getId(), context));
        }
        target.setComponents(mapped);
    }

    private static org.rutebanken.sobek.model.vehicle.TrainComponent componentToSobek(TrainComponent source, String ownerId, MappingContext context) {
        if (source.getTrainElement() != null) {
            throw new NetexMappingException("Train " + ownerId + " has a TrainElement given in full. Put it in the trainElementTypes of the frame, and refer to it with a TrainElementRef.");
        }
        var target = new org.rutebanken.sobek.model.vehicle.TrainComponent();
        target.setNetexId(idToKeep(source.getId(), target.getClass(), context));
        target.setLabel(STRINGS.mapToSobek(source.getLabel()));
        target.setDescription(STRINGS.mapToSobek(source.getDescription()));
        target.setOperationalOrientation(orientationToSobek(source.getOperationalOrientation()));
        TrainComponentCouplingStructure coupling = source.getForwardCoupling();
        if (coupling != null && coupling.getThroughAccess() != null) {
            target.setForwardCouplingThroughAccess(ThroughAccessEnumeration.valueOf(coupling.getThroughAccess().name()));
        }

        // The TrainRef beside the element is the train that owns the component, which the list it is in already says
        VersionOfObjectRefStructure elementRef;
        if (source.getTrainElementRef() != null) {
            elementRef = source.getTrainElementRef();
        } else if (source.getTrainElementTypeRef() != null && source.getTrainElementTypeRef().getValue() != null) {
            elementRef = source.getTrainElementTypeRef().getValue();
        } else {
            throw new NetexMappingException("A component of train " + ownerId + " has no TrainElementRef.");
        }
        target.setTrainElement(resolve(elementRef, org.rutebanken.sobek.model.vehicle.TrainElement.class, "Train element", ownerId, context));
        return target;
    }

    private static TrainInCompoundTrain componentToSobek(TrainInCompoundTrain_VersionedChildStructure source, String ownerId, MappingContext context) {
        if (source.getTrain() != null || source.getTrainElementType() != null) {
            throw new NetexMappingException("Compound train " + ownerId + " has a train or train element type given in full. Refer to a train with a TrainRef.");
        }
        var target = new TrainInCompoundTrain();
        target.setNetexId(idToKeep(source.getId(), target.getClass(), context));
        target.setLabel(STRINGS.mapToSobek(source.getLabel()));
        target.setDescription(STRINGS.mapToSobek(source.getDescription()));
        target.setOperationalOrientation(orientationToSobek(source.getOperationalOrientation()));

        if (source.getTrainRef() == null || source.getTrainRef().getValue() == null) {
            throw new NetexMappingException("A component of compound train " + ownerId + " has no TrainRef. "
                    + "Only trains can be put in a compound train.");
        }
        target.setTrain(resolve(source.getTrainRef().getValue(), org.rutebanken.sobek.model.vehicle.Train.class, "Train", ownerId, context));
        return target;
    }

    /** The id of a component in a codespace Sobek accepts; null (the saver assigns one) for any other id. Components have no key values to keep the original in. */
    private static String idToKeep(String id, Class<?> type, MappingContext context) {
        if (id == null || context.getValidPrefixList().getCodespaceTypeOf(id, type) == CodespaceType.EXTERNAL) {
            return null;
        }
        return id.trim();
    }

    private static <T extends org.rutebanken.sobek.model.DataManagedObjectStructure> T resolve(
            VersionOfObjectRefStructure ref, Class<T> type, String typeName, String ownerId, MappingContext context) {
        T resolved;
        try {
            resolved = ReferenceMapper.resolveReference(ref, type, context);
        } catch (ClassCastException e) {
            throw new NetexMappingException(ref.getRef() + ", referred to by " + ownerId + ", is not a " + typeName.toLowerCase() + ".");
        }
        if (resolved == null) {
            throw new NetexMappingException(typeName + " " + ref.getRef() + ", referred to by " + ownerId + ", was not found. "
                    + "Import it first, or in the same file.");
        }
        return resolved;
    }

    private static <T> List<T> inOrder(List<T> components, Function<T, BigInteger> order) {
        // Items without an order keep their position in the file. The sort is stable.
        return IntStream.range(0, components.size()).boxed()
                .sorted(Comparator.comparingLong(i -> order.apply(components.get(i)) == null
                        ? i + 1L : order.apply(components.get(i)).longValueExact()))
                .map(components::get)
                .toList();
    }

    private static VehicleOrientationEnumeration orientationToSobek(org.entur.netex2.model.VehicleOrientationEnumeration orientation) {
        return orientation == null ? null : VehicleOrientationEnumeration.valueOf(orientation.name());
    }

    private static org.entur.netex2.model.VehicleOrientationEnumeration orientationToNetex(VehicleOrientationEnumeration orientation) {
        return orientation == null ? null : org.entur.netex2.model.VehicleOrientationEnumeration.valueOf(orientation.name());
    }

    // ----- Sobek to NeTEx -----

    public static void toNetex(org.rutebanken.sobek.model.vehicle.Train source, Train target) {
        if (source.getNumberOfCars() != null || source.getTrainSizeType() != null) {
            target.setTrainSize(new TrainSizeStructure()
                    .withNumberOfCars(source.getNumberOfCars() == null ? null : BigInteger.valueOf(source.getNumberOfCars()))
                    .withTrainSizeType(source.getTrainSizeType() == null ? null
                            : org.entur.netex2.model.TrainSizeEnumeration.valueOf(source.getTrainSizeType().name())));
        }
        if (source.getComponents() == null || source.getComponents().isEmpty()) {
            return; // the schema requires at least one component in a container, so an empty one is left out
        }
        TrainComponents_RelStructure components = new TrainComponents_RelStructure();
        List<org.rutebanken.sobek.model.vehicle.TrainComponent> sobekComponents = source.getComponents();
        for (int i = 0; i < sobekComponents.size(); i++) {
            components.getTrainComponentRefOrTrainComponent().add(componentToNetex(sobekComponents.get(i), i + 1));
        }
        target.setComponents(components);
    }

    public static void toNetex(org.rutebanken.sobek.model.vehicle.CompoundTrain source, CompoundTrain target) {
        if (source.getComponents() == null || source.getComponents().isEmpty()) {
            return;
        }
        TrainsInCompoundTrain_RelStructure components = new TrainsInCompoundTrain_RelStructure();
        List<TrainInCompoundTrain> sobekComponents = source.getComponents();
        for (int i = 0; i < sobekComponents.size(); i++) {
            components.getTrainInCompoundTrain().add(componentToNetex(sobekComponents.get(i), source.getNetexId(), i + 1));
        }
        target.setComponents(components);
    }

    private static TrainComponent componentToNetex(org.rutebanken.sobek.model.vehicle.TrainComponent source, int order) {
        TrainComponent target = new TrainComponent();
        target.setId(source.getNetexId());
        target.setVersion(String.valueOf(source.getVersion()));
        target.setOrder(BigInteger.valueOf(order));
        target.setLabel(STRINGS.mapToNetex(source.getLabel()));
        target.setDescription(STRINGS.mapToNetex(source.getDescription()));
        target.setOperationalOrientation(orientationToNetex(source.getOperationalOrientation()));
        if (source.getForwardCouplingThroughAccess() != null) {
            target.setForwardCoupling(new TrainComponentCouplingStructure()
                    .withThroughAccess(org.entur.netex2.model.ThroughAccessEnumeration.valueOf(source.getForwardCouplingThroughAccess().name())));
        }
        if (source.getTrainElement() != null) {
            target.setTrainElementRef(ReferenceMapper.createReference(source.getTrainElement(), TrainElementTypeRefStructure.class));
        }
        return target;
    }

    private static TrainInCompoundTrain_VersionedChildStructure componentToNetex(TrainInCompoundTrain source, String ownerId, int order) {
        TrainInCompoundTrain_VersionedChildStructure target = new TrainInCompoundTrain_VersionedChildStructure();
        target.setId(source.getNetexId());
        target.setVersion(String.valueOf(source.getVersion()));
        target.setOrder(BigInteger.valueOf(order));
        target.setLabel(STRINGS.mapToNetex(source.getLabel()));
        target.setDescription(STRINGS.mapToNetex(source.getDescription()));
        target.setOperationalOrientation(orientationToNetex(source.getOperationalOrientation()));
        if (source.getTrain() != null) {
            target.setTrainRef(OBJECT_FACTORY.createTrainRef(ReferenceMapper.createReference(source.getTrain(), TrainRefStructure.class)));
        }
        return target;
    }
}

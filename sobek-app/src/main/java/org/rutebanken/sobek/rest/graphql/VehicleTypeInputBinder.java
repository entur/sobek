package org.rutebanken.sobek.rest.graphql;

import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ValidationException;
import org.hibernate.Hibernate;
import org.rutebanken.sobek.graphql.converter.VehicleTypeNeTExIdConverter;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;
import org.rutebanken.sobek.model.vehicle.CompoundTrain;
import org.rutebanken.sobek.model.vehicle.ThroughAccessEnumeration;
import org.rutebanken.sobek.model.vehicle.Train;
import org.rutebanken.sobek.model.vehicle.TrainComponent;
import org.rutebanken.sobek.model.vehicle.TrainElement;
import org.rutebanken.sobek.model.vehicle.TrainInCompoundTrain;
import org.rutebanken.sobek.model.vehicle.VehicleOrientationEnumeration;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.netex.id.CodespaceType;
import org.rutebanken.sobek.netex.id.ValidPrefixList;
import org.rutebanken.sobek.repository.VehicleTypeRepository;
import org.springframework.core.ResolvableType;
import org.springframework.graphql.data.GraphQlArgumentBinder;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Turns the input of createOrUpdateVehicleType into a VehicleType, a Train or a CompoundTrain, as its subtype says.
 * <p>
 * The fields that all subtypes share are bound by Spring GraphQL as before. The components are built here, as the
 * input names them differently from the entities (a train and a compound train both have "components" in the model).
 * Components are replaced as a whole: what is not in the input is not in the new version.
 */
@Component
public class VehicleTypeInputBinder {

    private static final String INPUT = "input";

    private final GraphQlArgumentBinder argumentBinder = new GraphQlArgumentBinder();
    private final VehicleTypeRepository vehicleTypeRepository;
    private final VehicleTypeNeTExIdConverter vehicleTypeIdConverter;
    private final ValidPrefixList validPrefixList;

    public VehicleTypeInputBinder(VehicleTypeRepository vehicleTypeRepository, VehicleTypeNeTExIdConverter vehicleTypeIdConverter, ValidPrefixList validPrefixList) {
        this.vehicleTypeRepository = vehicleTypeRepository;
        this.vehicleTypeIdConverter = vehicleTypeIdConverter;
        this.validPrefixList = validPrefixList;
    }

    public VehicleType bind(DataFetchingEnvironment env) {
        Map<String, Object> raw = env.getArgument(INPUT);
        VehicleTypeSubtype subtype = raw.get("subtype") == null ? VehicleTypeSubtype.VEHICLE_TYPE : VehicleTypeSubtype.valueOf(raw.get("subtype").toString());
        rejectFieldsOfOtherSubtypes(raw, subtype);

        VehicleType bound;
        try {
            bound = (VehicleType) argumentBinder.bind(env, INPUT, ResolvableType.forClass(subtype.entityClass()));
        } catch (BindException e) {
            throw new ValidationException("Invalid vehicle type input: " + e.getMessage(), e);
        }
        addKeyValues(bound, raw);
        if (bound instanceof Train train) {
            train.setComponents(trainComponents(rawList(raw, "trainComponents")));
        } else if (bound instanceof CompoundTrain compoundTrain) {
            compoundTrain.setComponents(trainsInCompoundTrain(rawList(raw, "compoundTrainComponents")));
        }
        return bound;
    }

    /**
     * The key values of the input are not bound to the entity, as it has no setter for them. As for any new version, the
     * key values of the previous version that the input does not give again are kept.
     */
    private static void addKeyValues(VehicleType vehicleType, Map<String, Object> raw) {
        for (Map<String, Object> keyValue : rawList(raw, "keyValues")) {
            vehicleType.removeKeyValue((String) keyValue.get("key"));
            vehicleType.addKeyValue((String) keyValue.get("key"), (String) keyValue.get("value"));
        }
    }

    private static void rejectFieldsOfOtherSubtypes(Map<String, Object> raw, VehicleTypeSubtype subtype) {
        if (subtype != VehicleTypeSubtype.TRAIN) {
            rejectFields(raw, subtype, "numberOfCars", "trainSizeType", "trainComponents");
        }
        if (subtype != VehicleTypeSubtype.COMPOUND_TRAIN) {
            rejectFields(raw, subtype, "compoundTrainComponents");
        }
        if (subtype != VehicleTypeSubtype.TRAIN_ELEMENT) {
            rejectFields(raw, subtype, "trainElementType");
        }
    }

    private static void rejectFields(Map<String, Object> raw, VehicleTypeSubtype subtype, String... fields) {
        for (String field : fields) {
            if (raw.get(field) != null) {
                throw new ValidationException("'" + field + "' cannot be given for subtype " + subtype);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> rawList(Map<String, Object> raw, String field) {
        Object value = raw.get(field);
        return value == null ? List.of() : (List<Map<String, Object>>) value;
    }

    private List<TrainComponent> trainComponents(List<Map<String, Object>> rawComponents) {
        List<TrainComponent> components = new ArrayList<>();
        for (Map<String, Object> rawComponent : rawComponents) {
            TrainComponent component = new TrainComponent();
            component.setNetexId(idToKeep(rawComponent, TrainComponent.class));
            component.setLabel(multilingual(rawComponent.get("label")));
            component.setDescription(multilingual(rawComponent.get("description")));
            component.setTrainElement(findTrainElement(rawComponent.get("trainElement")));
            component.setOperationalOrientation(enumValue(VehicleOrientationEnumeration.class, rawComponent.get("operationalOrientation")));
            component.setForwardCouplingThroughAccess(enumValue(ThroughAccessEnumeration.class, rawComponent.get("forwardCouplingThroughAccess")));
            components.add(component);
        }
        return components;
    }

    private List<TrainInCompoundTrain> trainsInCompoundTrain(List<Map<String, Object>> rawComponents) {
        List<TrainInCompoundTrain> components = new ArrayList<>();
        for (Map<String, Object> rawComponent : rawComponents) {
            TrainInCompoundTrain component = new TrainInCompoundTrain();
            component.setNetexId(idToKeep(rawComponent, TrainInCompoundTrain.class));
            component.setLabel(multilingual(rawComponent.get("label")));
            component.setDescription(multilingual(rawComponent.get("description")));
            component.setOperationalOrientation(enumValue(VehicleOrientationEnumeration.class, rawComponent.get("operationalOrientation")));
            component.setTrain(findOfType(rawComponent.get("train"), Train.class, "Compound train", "train", "trains"));
            components.add(component);
        }
        return components;
    }

    private TrainElement findTrainElement(Object reference) {
        return findOfType(reference, TrainElement.class, "Train", "train element", "train elements");
    }

    /**
     * The vehicle type a reference names, in its latest version. It is an error if there is none, or it is not of the
     * type the holder can hold (a train holds train elements, a compound train holds trains).
     */
    @SuppressWarnings("unchecked")
    private <T extends VehicleType> T findOfType(Object reference, Class<T> type, String holder, String what, String whatPlural) {
        String netexId = (String) ((Map<String, Object>) reference).get("netexId");
        VehicleType asGiven = new VehicleType();
        asGiven.setNetexId(netexId);
        String convertedId = vehicleTypeIdConverter.convertIncomingId(asGiven).getNetexId();
        VehicleType found = vehicleTypeRepository.findFirstByNetexIdOrderByVersionDesc(convertedId);
        if (found == null) {
            throw new ValidationException(holder + " refers to a " + what + " that is not found in the database: " + netexId);
        }
        Object real = Hibernate.unproxy(found);
        if (!type.isInstance(real)) {
            throw new ValidationException(holder + " refers to " + netexId + ", which is a " + VehicleTypeSubtype.of(found)
                    + ". Only " + whatPlural + " can be put in a " + holder.toLowerCase() + ".");
        }
        return type.cast(real);
    }

    /** An id in a codespace Sobek accepts is kept; any other, or none, is replaced by an id assigned on save. */
    private String idToKeep(Map<String, Object> rawComponent, Class<?> type) {
        String id = (String) rawComponent.get("netexId");
        if (id == null || validPrefixList.getCodespaceTypeOf(id, type) == CodespaceType.EXTERNAL) {
            return null;
        }
        return id.trim();
    }

    @SuppressWarnings("unchecked")
    private static EmbeddableMultilingualString multilingual(Object raw) {
        if (raw == null) {
            return null;
        }
        Map<String, Object> map = (Map<String, Object>) raw;
        return new EmbeddableMultilingualString((String) map.get("value"), (String) map.get("lang"));
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, Object raw) {
        return raw == null ? null : Enum.valueOf(type, raw.toString());
    }
}

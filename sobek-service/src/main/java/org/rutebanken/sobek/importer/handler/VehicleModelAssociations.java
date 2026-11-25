package org.rutebanken.sobek.importer.handler;

import jakarta.validation.ValidationException;
import org.entur.netex2.model.ResourceFrame;
import org.entur.netex2.model.VehicleModel;
import org.entur.netex2.model.VehicleType_VersionStructure;
import org.rutebanken.sobek.netex.mapping.mapstruct.MultilingualStringMapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Which vehicle model of an incoming resource frame belongs to which vehicle type. Sobek keeps no vehicle model of its
 * own: what NeTEx keeps in a model is kept in the vehicle type, so a model is only meaningful together with its type.
 * <p>
 * A model belongs to the type it has a transport type reference to, and to the types that are classified as it. The data
 * is rejected when a model belongs to no type, when it belongs to more than one, and when a type has more than one
 * model: each of these would mean losing or inventing data.
 */
final class VehicleModelAssociations {

    // Only has default methods, so it needs no generated implementation
    private static final MultilingualStringMapper STRINGS = new MultilingualStringMapper() { };

    private final Map<String, VehicleModel> modelsById;
    private final Map<String, String> typeIdByModelId;
    private final Map<String, String> modelIdByTypeId;

    private VehicleModelAssociations(Map<String, VehicleModel> modelsById,
                                     Map<String, String> typeIdByModelId,
                                     Map<String, String> modelIdByTypeId) {
        this.modelsById = modelsById;
        this.typeIdByModelId = typeIdByModelId;
        this.modelIdByTypeId = modelIdByTypeId;
    }

    /**
     * @throws ValidationException if the models of the frame do not each belong to exactly one vehicle type
     */
    static VehicleModelAssociations of(ResourceFrame frame) {
        Map<String, VehicleModel> modelsById = new LinkedHashMap<>();
        if (frame.getVehicleModels() != null) {
            for (VehicleModel model : frame.getVehicleModels().getVehicleModel()) {
                if (model.getId() == null) {
                    throw new ValidationException("A vehicle model has no id");
                }
                if (modelsById.put(model.getId(), model) != null) {
                    throw new ValidationException("The vehicle model " + model.getId() + " is in the delivery more than once");
                }
            }
        }

        Map<String, Set<String>> typesOfModel = new HashMap<>();
        Map<String, Set<String>> modelsOfType = new HashMap<>();
        for (VehicleModel model : modelsById.values()) {
            if (model.getTransportTypeRef() != null && model.getTransportTypeRef().getValue() != null
                    && model.getTransportTypeRef().getValue().getRef() != null) {
                associate(typesOfModel, modelsOfType, model.getId(), model.getTransportTypeRef().getValue().getRef());
            }
        }
        if (frame.getVehicleTypes() != null) {
            frame.getVehicleTypes().getTransportType().stream()
                    .map(jaxbElement -> jaxbElement.getValue())
                    .filter(VehicleType_VersionStructure.class::isInstance)
                    .map(VehicleType_VersionStructure.class::cast)
                    .filter(type -> type.getClassifiedAsRef() != null && type.getClassifiedAsRef().getRef() != null)
                    .forEach(type -> {
                        String modelId = type.getClassifiedAsRef().getRef();
                        if (!modelsById.containsKey(modelId)) {
                            throw new ValidationException("The vehicle type " + type.getId() + " is classified as the vehicle model "
                                    + modelId + ", which is not in the delivery. A vehicle model is kept in its vehicle type, so it must be sent with it.");
                        }
                        associate(typesOfModel, modelsOfType, modelId, type.getId());
                    });
        }

        Map<String, String> typeIdByModelId = new HashMap<>();
        for (String modelId : modelsById.keySet()) {
            Set<String> typeIds = typesOfModel.getOrDefault(modelId, Set.of());
            if (typeIds.isEmpty()) {
                throw new ValidationException("The vehicle model " + modelId + " does not belong to a vehicle type: it has no transport type reference, "
                        + "and no vehicle type is classified as it. A vehicle model is kept in its vehicle type.");
            }
            if (typeIds.size() > 1) {
                throw new ValidationException("The vehicle model " + modelId + " belongs to more than one vehicle type: " + String.join(", ", typeIds)
                        + ". A vehicle type has its own vehicle model.");
            }
            typeIdByModelId.put(modelId, typeIds.iterator().next());
        }

        Map<String, String> modelIdByTypeId = new HashMap<>();
        modelsOfType.forEach((typeId, modelIds) -> {
            if (modelIds.size() > 1) {
                throw new ValidationException("The vehicle type " + typeId + " has more than one vehicle model: " + String.join(", ", modelIds));
            }
            modelIdByTypeId.put(typeId, modelIds.iterator().next());
        });

        return new VehicleModelAssociations(modelsById, typeIdByModelId, modelIdByTypeId);
    }

    private static void associate(Map<String, Set<String>> typesOfModel, Map<String, Set<String>> modelsOfType, String modelId, String typeId) {
        typesOfModel.computeIfAbsent(modelId, k -> new HashSet<>()).add(typeId);
        modelsOfType.computeIfAbsent(typeId, k -> new HashSet<>()).add(modelId);
    }

    /** The model of the vehicle type with the id the client sent, if the frame has one. */
    Optional<VehicleModel> modelOfType(String clientTypeId) {
        return Optional.ofNullable(modelIdByTypeId.get(clientTypeId)).map(modelsById::get);
    }

    /** The id of the vehicle type the model, as the client sent its id, belongs to, if the frame has the model. */
    Optional<String> typeIdOfModel(String clientModelId) {
        return Optional.ofNullable(typeIdByModelId.get(clientModelId));
    }

    /** The ids, as the client sent them, of the vehicle types that have a model in the frame. */
    Set<String> typeIds() {
        return modelIdByTypeId.keySet();
    }

    /** Copies what the model has of its own to the Sobek vehicle type. */
    static void applyTo(VehicleModel model, org.rutebanken.sobek.model.vehicle.VehicleType type) {
        type.setManufacturer(STRINGS.mapToSobek(model.getManufacturer()));
        type.setRange(model.getRange());
        type.setFullCharge(model.getFullCharge());
    }
}

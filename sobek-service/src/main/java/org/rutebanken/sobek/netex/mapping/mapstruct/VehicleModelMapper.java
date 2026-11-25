package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.entur.netex2.model.ObjectFactory;
import org.entur.netex2.model.TransportTypeRefStructure;
import org.entur.netex2.model.VehicleModel;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.netex.id.VehicleModelId;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Sobek has no vehicle model of its own: what NeTEx keeps in a vehicle model (manufacturer, range, full charge) is kept
 * in the vehicle type. This maps the vehicle model of a vehicle type to the NeTEx element, for the export, with an id
 * derived from the id of the type.
 */
@Component
public class VehicleModelMapper {

    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    // Only has default methods, so it needs no generated implementation
    private static final MultilingualStringMapper STRINGS = new MultilingualStringMapper() { };

    /**
     * The NeTEx vehicle model of the type, or empty if the type has no model data.
     */
    public Optional<VehicleModel> mapToNetex(VehicleType source) {
        if (source == null || !source.hasVehicleModelData() || source.getNetexId() == null) {
            return Optional.empty();
        }

        VehicleModel model = new VehicleModel()
                .withId(VehicleModelId.forVehicleType(source.getNetexId()))
                .withVersion(String.valueOf(source.getVersion()));

        // The type is what has a name: the model is named after it
        model.withName(STRINGS.mapToNetex(source.getName()));
        model.withManufacturer(STRINGS.mapToNetex(source.getManufacturer()));
        model.withRange(source.getRange());
        model.withFullCharge(source.getFullCharge());

        TransportTypeRefStructure typeRef = ReferenceMapper.createReference(source, TransportTypeRefStructure.class);
        model.withTransportTypeRef(OBJECT_FACTORY.createTransportTypeRef(typeRef));
        return Optional.of(model);
    }

    /**
     * Adds the vehicle model of the type, if it has one, to the vehicle models of the frame, unless the frame has it.
     * For a response that has vehicles: a vehicle refers to the model of its type, and the model must be in the frame.
     */
    public void addToFrame(org.entur.netex2.model.ResourceFrame frame, VehicleType type) {
        mapToNetex(type).ifPresent(model -> {
            if (frame.getVehicleModels() == null) {
                frame.setVehicleModels(new org.entur.netex2.model.VehicleModelsInFrame_RelStructure());
            }
            var models = frame.getVehicleModels().getVehicleModel();
            if (models.stream().noneMatch(existing -> model.getId().equals(existing.getId()))) {
                models.add(model);
            }
        });
    }
}

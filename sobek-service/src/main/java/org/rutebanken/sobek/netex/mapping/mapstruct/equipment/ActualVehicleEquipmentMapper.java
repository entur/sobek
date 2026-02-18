package org.rutebanken.sobek.netex.mapping.mapstruct.equipment;

import jakarta.xml.bind.JAXBElement;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.rutebanken.netex.model.*;
import org.rutebanken.sobek.model.vehicle.Equipment;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.ReferenceMapper;

import java.util.List;
import java.util.Objects;

@Mapper(
        config = SobekMapperConfig.class,
        uses = {
                DataManagedObjectStructureMapper.class
        }
)
public interface ActualVehicleEquipmentMapper {

    default List<Equipment> mapActualEquipmentsNeTEx2Sobek(ActualVehicleEquipments_RelStructure actualVehicleEquipments, @Context MappingContext context) {
        if(actualVehicleEquipments != null &&
                actualVehicleEquipments.getActualVehicleEquipment() != null &&
                !actualVehicleEquipments.getActualVehicleEquipment().isEmpty()) {

            return actualVehicleEquipments.getActualVehicleEquipment().stream().map(av -> mapActualEquipmentNeTEx2Sobek(av, context))
                    .toList();
        }
        return null;
    }

    private Equipment mapActualEquipmentNeTEx2Sobek(ActualVehicleEquipment_VersionStructure actualVehicleEquipmentVersionStructure, MappingContext context) {
        if (actualVehicleEquipmentVersionStructure.getEquipmentRef() == null || actualVehicleEquipmentVersionStructure.getEquipmentRef().getValue() == null) {
            return null;
        }

        Object refValue = actualVehicleEquipmentVersionStructure.getEquipmentRef().getValue();

        return switch (refValue) {
            case org.rutebanken.netex.model.AccessVehicleEquipmentRefStructure ref ->
                    ReferenceMapper.resolveReference(ref, org.rutebanken.sobek.model.vehicle.AccessVehicleEquipment.class, context);
            case org.rutebanken.netex.model.BedEquipmentRefStructure ref ->
                    ReferenceMapper.resolveReference(ref, org.rutebanken.sobek.model.vehicle.BedEquipment.class, context);
            case org.rutebanken.netex.model.EntranceEquipmentRefStructure ref ->
                    ReferenceMapper.resolveReference(ref, org.rutebanken.sobek.model.vehicle.EntranceEquipment.class, context);
            case org.rutebanken.netex.model.LuggageSpotEquipmentRefStructure ref ->
                    ReferenceMapper.resolveReference(ref, org.rutebanken.sobek.model.vehicle.LuggageSpotEquipment.class, context);
            case org.rutebanken.netex.model.SeatEquipmentRefStructure ref ->
                    ReferenceMapper.resolveReference(ref, org.rutebanken.sobek.model.vehicle.SeatEquipment.class, context);
            case org.rutebanken.netex.model.SpotEquipmentRefStructure ref ->
                    ReferenceMapper.resolveReference(ref, org.rutebanken.sobek.model.vehicle.SpotEquipment.class, context);
            case org.rutebanken.netex.model.StaircaseEquipmentRefStructure ref ->
                    ReferenceMapper.resolveReference(ref, org.rutebanken.sobek.model.vehicle.StaircaseEquipment.class, context);
            default -> null;
        };
    }

    default ActualVehicleEquipments_RelStructure mapActualEquipmentsSobek2NeTEx(List<Equipment> sobekEquipments) {
        if(sobekEquipments != null && !sobekEquipments.isEmpty()) {
            ActualVehicleEquipments_RelStructure actualVehicleEquipments = new ActualVehicleEquipments_RelStructure();
            actualVehicleEquipments.withActualVehicleEquipment(
                    sobekEquipments.stream().map(e -> new ActualVehicleEquipment_VersionStructure().withEquipmentRef(createNeTExEquipmentRef(e)))
                            .filter(Objects::nonNull)
                            .toList());
            return actualVehicleEquipments;
        }
        return null;
    }

    private JAXBElement<? extends EquipmentRefStructure> createNeTExEquipmentRef(org.rutebanken.sobek.model.vehicle.Equipment equipment) {
        ObjectFactory objectFactory = new ObjectFactory();
        return switch (equipment) {
            case org.rutebanken.sobek.model.vehicle.SeatEquipment seatEquipment -> objectFactory.createSeatEquipmentRef(new SeatEquipmentRefStructure().withRef(seatEquipment.getNetexId()));
            case org.rutebanken.sobek.model.vehicle.BedEquipment bedEquipment -> objectFactory.createBedEquipmentRef(new BedEquipmentRefStructure().withRef(bedEquipment.getNetexId()));
            case org.rutebanken.sobek.model.vehicle.AccessVehicleEquipment accessVehicleEquipment -> objectFactory.createAccessVehicleEquipmentRef(new AccessVehicleEquipmentRefStructure().withRef(accessVehicleEquipment.getNetexId()));
            case org.rutebanken.sobek.model.vehicle.EntranceEquipment entranceEquipment -> objectFactory.createEntranceEquipmentRef(new EntranceEquipmentRefStructure().withRef(entranceEquipment.getNetexId()));
            case org.rutebanken.sobek.model.vehicle.SpotEquipment spotEquipment -> objectFactory.createSpotEquipmentRef(new SpotEquipmentRefStructure().withRef(spotEquipment.getNetexId()));
            case org.rutebanken.sobek.model.vehicle.LuggageSpotEquipment luggageSpotEquipment -> objectFactory.createLuggageSpotEquipmentRef(new LuggageSpotEquipmentRefStructure().withRef(luggageSpotEquipment.getNetexId()));
            case org.rutebanken.sobek.model.vehicle.StaircaseEquipment staircaseEquipment -> objectFactory.createStaircaseEquipmentRef(new StaircaseEquipmentRefStructure().withRef(staircaseEquipment.getNetexId()));
            default -> null;
        };

    }
}

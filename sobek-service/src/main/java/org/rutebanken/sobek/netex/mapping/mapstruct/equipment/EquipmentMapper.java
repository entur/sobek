package org.rutebanken.sobek.netex.mapping.mapstruct.equipment;

import jakarta.xml.bind.JAXBElement;
import org.mapstruct.*;
import org.rutebanken.netex.model.*;
import org.rutebanken.netex.model.ObjectFactory;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapper;

/**
 * MapStruct mapper for Equipment.
 * Handles mapping between NeTEx Equipment_VersionStructure and Sobek Equipment entity.
 */
@Mapper(
        config = SobekMapperConfig.class,
        uses = {
                DataManagedObjectStructureMapper.class
        }
)
public interface EquipmentMapper {

    default JAXBElement<? extends Equipment_VersionStructure> mapToNetexJaxbEquipment(Equipment_VersionStructure netexEquipment, MappingContext context) {
        if(netexEquipment == null) {
            return null;
        }
        ObjectFactory objectFactory = new ObjectFactory();
        return switch (netexEquipment) {
            case AccessVehicleEquipment accessVehicleEquipment -> objectFactory.createAccessVehicleEquipment(accessVehicleEquipment);
            case BedEquipment bedEquipment -> objectFactory.createBedEquipment(bedEquipment);
            case EntranceEquipment entranceEquipment -> objectFactory.createEntranceEquipment(entranceEquipment);
            case LuggageSpotEquipment luggageSpotEquipment -> objectFactory.createLuggageSpotEquipment(luggageSpotEquipment);
            case SeatEquipment seatEquipment -> objectFactory.createSeatEquipment(seatEquipment);
            case SpotEquipment spotEquipment -> objectFactory.createSpotEquipment(spotEquipment);
            case StaircaseEquipment staircaseEquipment -> objectFactory.createStaircaseEquipment(staircaseEquipment);
            default -> null;
        };
    }

    @DataManagedObjectStructureMapper.ToNetexMappings
    default Equipment_VersionStructure mapToNetexManual(
            org.rutebanken.sobek.model.vehicle.Equipment source,
            @Context MappingContext context
    ) {
        if(source == null) {
            return null;
        }
        return switch (source) {
            case org.rutebanken.sobek.model.vehicle.AccessVehicleEquipment accessVehicleEquipment -> context.getAccessVehicleEquipmentMapper().mapToNetex(accessVehicleEquipment, context);
            case org.rutebanken.sobek.model.vehicle.BedEquipment bedEquipment -> context.getBedEquipmentMapper().mapToNetex(bedEquipment, context);
            case org.rutebanken.sobek.model.vehicle.EntranceEquipment entranceEquipment -> context.getEntranceEquipmentMapper().mapToNetex(entranceEquipment, context);
            case org.rutebanken.sobek.model.vehicle.LuggageSpotEquipment luggageSpotEquipment -> context.getLuggageSpotEquipmentMapper().mapToNetex(luggageSpotEquipment, context);
            case org.rutebanken.sobek.model.vehicle.SeatEquipment seatEquipment -> context.getSeatEquipmentMapper().mapToNetex(seatEquipment, context);
            case org.rutebanken.sobek.model.vehicle.SpotEquipment spotEquipment -> context.getSpotEquipmentMapper().mapToNetex(spotEquipment, context);
            case org.rutebanken.sobek.model.vehicle.StaircaseEquipment staircaseEquipment -> context.getStaircaseEquipmentMapper().mapToNetex(staircaseEquipment, context);
            default -> null;
        };
    };

    @DataManagedObjectStructureMapper.ToSobekMappings
    default org.rutebanken.sobek.model.vehicle.Equipment mapToSobekManual(
            Equipment_VersionStructure source,
            @Context MappingContext context
    ) {
        return switch (source) {
            case AccessVehicleEquipment accessVehicleEquipment -> context.getAccessVehicleEquipmentMapper().mapToSobek(accessVehicleEquipment, context);
            case BedEquipment bedEquipment -> context.getBedEquipmentMapper().mapToSobek(bedEquipment, context);
            case EntranceEquipment entranceEquipment -> context.getEntranceEquipmentMapper().mapToSobek(entranceEquipment, context);
            case LuggageSpotEquipment luggageSpotEquipment -> context.getLuggageSpotEquipmentMapper().mapToSobek(luggageSpotEquipment, context);
            case SeatEquipment seatEquipment -> context.getSeatEquipmentMapper().mapToSobek(seatEquipment, context);
            case SpotEquipment spotEquipment -> context.getSpotEquipmentMapper().mapToSobek(spotEquipment, context);
            case StaircaseEquipment staircaseEquipment -> context.getStaircaseEquipmentMapper().mapToSobek(staircaseEquipment, context);
            default -> null;
        };
    };

    @AfterMapping
    default void afterMapToSobek(
            Equipment_VersionStructure source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.Equipment target,
            @Context MappingContext context
    ) {
        if (target != null) {
            context.getDataManagedObjectStructureMapper().afterMappingToSobek(source, target, context);
        }
    }
    @AfterMapping
    default void afterMapToNetex(
            org.rutebanken.sobek.model.vehicle.Equipment source,
            @MappingTarget Equipment_VersionStructure target,
            @Context MappingContext context
    ) {
        if (target != null) {
            context.getDataManagedObjectStructureMapper().afterMappingToNetex(source, target, context);
        }
    }
}
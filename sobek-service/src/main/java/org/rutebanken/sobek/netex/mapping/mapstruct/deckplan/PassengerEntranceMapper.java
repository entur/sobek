package org.rutebanken.sobek.netex.mapping.mapstruct.deckplan;

import org.mapstruct.*;
import org.rutebanken.netex.model.PassengerEntrance;
import org.rutebanken.netex.model.ObjectFactory;
import org.rutebanken.netex.model.DeckEntrances_RelStructure;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.PointRefStructureMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.PolygonMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.SimplePointMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.equipment.ActualVehicleEquipmentMapper;

import java.util.List;

/**
 * MapStruct mapper for PassengerEntrance.
 * Handles mapping between NeTEx PassengerEntrance and Sobek PassengerEntrance entity.
 */
@Mapper(
        config = SobekMapperConfig.class,
        uses = {
                DataManagedObjectStructureMapper.class,
                PointRefStructureMapper.class,
                PolygonMapper.class,
                SimplePointMapper.class,
                ActualVehicleEquipmentMapper.class
        }
)
public interface PassengerEntranceMapper {
    ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    /**
     * Maps from NeTEx PassengerEntrance to Sobek entity.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    @Mapping(target = "polygon", source = "polygon", qualifiedByName = "polygonTypeToPolygon")
    org.rutebanken.sobek.model.vehicle.PassengerEntrance mapToSobek(
            PassengerEntrance source,
            @Context MappingContext context
    );

    /**
     * Maps from Sobek entity back to NeTEx PassengerEntrance.
     */
    @DataManagedObjectStructureMapper.ToNetexMappings
    @Mapping(target = "sensorsInEntrance", ignore = true) // TODO: Implement when needed
    @Mapping(target = "typeOfDeckEntranceUsageRef", ignore = true) // TODO: Implement when needed
    PassengerEntrance mapToNetex(
            org.rutebanken.sobek.model.vehicle.PassengerEntrance source,
            @Context MappingContext context
    );

    /**
     * Updates an existing Sobek entity from NeTEx structure.
     */
    @DataManagedObjectStructureMapper.ToSobekMappings
    @Mapping(target = "actualVehicleEquipments", ignore = true)
    @Mapping(target = "polygon", source = "polygon", qualifiedByName = "polygonTypeToPolygon")
    void updateSobekFromNetex(
            PassengerEntrance source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.PassengerEntrance target,
            @Context MappingContext context
    );

    @AfterMapping
    default void afterMapToSobek(
            PassengerEntrance source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.PassengerEntrance target,
            @Context MappingContext context
    ) {
        if (target != null) {
            context.getDataManagedObjectStructureMapper().afterMappingToSobek(source, target, context);
        }
    }
    @AfterMapping
    default void afterMapToNetex(
            org.rutebanken.sobek.model.vehicle.PassengerEntrance source,
            @MappingTarget PassengerEntrance target,
            @Context MappingContext context
    ) {
        if (target != null) {
            context.getDataManagedObjectStructureMapper().afterMappingToNetex(source, target, context);
        }
    }

    /**
     * Maps a list of PassengerEntrances from Sobek to NeTEx RelStructure.
     */
    default DeckEntrances_RelStructure mapSobekListToNetexRelStructure(
            List<org.rutebanken.sobek.model.vehicle.PassengerEntrance> source,
            @Context MappingContext context
    ) {

        if (source == null) {
            return null;
        }

        if (source.isEmpty()) {
            return new DeckEntrances_RelStructure();
        }

        return new DeckEntrances_RelStructure().withDeckEntranceRefOrDeckEntrance_Dummy(source.stream()
                .map(sobekEntrance -> OBJECT_FACTORY.createPassengerEntrance(mapToNetex(sobekEntrance, context)))
                .collect(java.util.stream.Collectors.toList()));
    }

    /**
     * Maps a list of PassengerEntrances from NeTEx RelStructure to Sobek.
     */
    default List<org.rutebanken.sobek.model.vehicle.PassengerEntrance> mapNetexRelStructureToSobekList(
            DeckEntrances_RelStructure source,
            @Context MappingContext context
    ) {
        if (source == null || source.getDeckEntranceRefOrDeckEntrance_Dummy() == null) {
            return null;
        }

        return source.getDeckEntranceRefOrDeckEntrance_Dummy().stream()
                .map(netexEntrance -> mapToSobek((PassengerEntrance) netexEntrance.getValue(), context))// TODO: Each item could be either PassengerEntrance or PassengerEntranceRef
                .collect(java.util.stream.Collectors.toList());
    }
}
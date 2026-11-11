package org.rutebanken.sobek.netex.mapping.mapstruct;

import jakarta.xml.bind.JAXBElement;
import org.entur.netex2.model.Fleet;
import org.entur.netex2.model.ObjectFactory;
import org.entur.netex2.model.TransportTypeRefStructure;
import org.entur.netex2.model.TransportTypeRefs_RelStructure;
import org.entur.netex2.model.VehicleRefStructure;
import org.entur.netex2.model.Vehicles_RelStructure;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.netex.mapping.NetexMappingException;
import org.rutebanken.sobek.netex.mapping.config.SobekMapperConfig;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;

import java.util.List;

/**
 * MapStruct mapper for Fleet.
 * <p>
 * The members of a fleet are vehicles that Sobek already has, so they are given as VehicleRef, never as a Vehicle
 * inside the Fleet. The transport types are references to vehicle types. TransportOrganisationRef and TypeOfFleetRef are
 * not supported yet, and are left out of the export and ignored on import.
 */
@Mapper(
        config = SobekMapperConfig.class,
        uses = {
                DataManagedObjectStructureMapper.class,
                EntityInVersionMapper.class
        }
)
public interface FleetMapper {

    ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    @DataManagedObjectStructureMapper.ToSobekMappings
    @Mapping(target = "members", ignore = true) // Resolved in @AfterMapping
    @Mapping(target = "transportTypes", ignore = true) // Resolved in @AfterMapping
    org.rutebanken.sobek.model.vehicle.Fleet mapToSobek(
            Fleet source,
            @Context MappingContext context
    );

    @DataManagedObjectStructureMapper.ToNetexMappings
    @Mapping(target = "members", ignore = true) // Created in @AfterMapping
    @Mapping(target = "transportTypes", ignore = true) // Created in @AfterMapping
    @Mapping(target = "transportOrganisationRef", ignore = true)
    @Mapping(target = "typeOfFleetRef", ignore = true)
    @Mapping(target = "brandingRef", ignore = true)
    Fleet mapToNetex(
            org.rutebanken.sobek.model.vehicle.Fleet source,
            @Context MappingContext context
    );

    @AfterMapping
    default void afterMapToSobek(
            Fleet source,
            @MappingTarget org.rutebanken.sobek.model.vehicle.Fleet target,
            @Context MappingContext context
    ) {
        target.getMembers().clear();
        target.getTransportTypes().clear();

        if (source.getMembers() != null) {
            for (JAXBElement<?> member : source.getMembers().getVehicleRefOrVehicle()) {
                if (!(member.getValue() instanceof VehicleRefStructure ref)) {
                    throw new NetexMappingException("Fleet " + source.getId() + " contains a Vehicle. The members of a fleet must be given as VehicleRef, to vehicles that already exist.");
                }
                Vehicle vehicle = ReferenceMapper.resolveReference(ref, Vehicle.class, context);
                if (vehicle == null) {
                    throw new NetexMappingException("Fleet " + source.getId() + " has the member " + ref.getRef() + ", which does not exist.");
                }
                if (!target.getMembers().contains(vehicle)) {
                    target.getMembers().add(vehicle);
                }
            }
        }

        if (source.getTransportTypes() != null) {
            for (JAXBElement<? extends TransportTypeRefStructure> transportTypeRef : source.getTransportTypes().getTransportTypeRef()) {
                TransportTypeRefStructure ref = transportTypeRef.getValue();
                VehicleType vehicleType = ReferenceMapper.resolveReference(ref, VehicleType.class, context);
                if (vehicleType == null) {
                    throw new NetexMappingException("Fleet " + source.getId() + " has the transport type " + ref.getRef() + ", which does not exist.");
                }
                if (!target.getTransportTypes().contains(vehicleType)) {
                    target.getTransportTypes().add(vehicleType);
                }
            }
        }

        context.getOwnedEntityMapper().updateSobekFromNetex(target, context);
    }

    @AfterMapping
    default void afterMapToNetex(
            org.rutebanken.sobek.model.vehicle.Fleet source,
            @MappingTarget Fleet target,
            @Context MappingContext context
    ) {
        if (source.getMembers() != null && !source.getMembers().isEmpty()) {
            Vehicles_RelStructure members = new Vehicles_RelStructure();
            for (Vehicle vehicle : source.getMembers()) {
                VehicleRefStructure ref = ReferenceMapper.createReference(vehicle, VehicleRefStructure.class);
                if (ref != null) {
                    members.getVehicleRefOrVehicle().add(OBJECT_FACTORY.createVehicleRef(ref));
                }
            }
            target.withMembers(members);
        }

        if (source.getTransportTypes() != null && !source.getTransportTypes().isEmpty()) {
            TransportTypeRefs_RelStructure transportTypes = new TransportTypeRefs_RelStructure();
            for (VehicleType vehicleType : source.getTransportTypes()) {
                TransportTypeRefStructure ref = ReferenceMapper.createReference(vehicleType, TransportTypeRefStructure.class);
                if (ref != null) {
                    transportTypes.getTransportTypeRef().add(OBJECT_FACTORY.createTransportTypeRef(ref));
                }
            }
            target.withTransportTypes(transportTypes);
        }
    }

    List<org.rutebanken.sobek.model.vehicle.Fleet> mapAsList(List<Fleet> sourceList, @Context MappingContext context);
}

package org.rutebanken.sobek.netex.mapping.mapstruct;

import jakarta.xml.bind.JAXBElement;
import org.entur.netex2.model.Fleet;
import org.entur.netex2.model.KeyListStructure;
import org.entur.netex2.model.KeyValueStructure;
import org.entur.netex2.model.ObjectFactory;
import org.entur.netex2.model.OperatorRefStructure;
import org.entur.netex2.model.TransportTypeRefStructure;
import org.entur.netex2.model.TransportTypeRefs_RelStructure;
import org.entur.netex2.model.VehicleRefStructure;
import org.entur.netex2.model.Vehicles_RelStructure;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.rutebanken.sobek.model.CustomKeyValueTypes;
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

    /**
     * Maps everything but the contract number, which is in the key values until {@link #mapToSobek} has moved it.
     */
    @Named("fleetWithKeyValues")
    @DataManagedObjectStructureMapper.ToSobekMappings
    @Mapping(target = "members", ignore = true) // Resolved in @AfterMapping
    @Mapping(target = "transportTypes", ignore = true) // Resolved in @AfterMapping
    @Mapping(target = "transportOrganisationRef", ignore = true) // Set in @AfterMapping
    @Mapping(target = "contractNumber", ignore = true) // Moved from the key values by mapToSobek
    org.rutebanken.sobek.model.vehicle.Fleet mapFleetToSobek(
            Fleet source,
            @Context MappingContext context
    );

    /**
     * The contract number is a key value in NeTEx, and a property of its own in Sobek. The key values are not complete
     * until the mapping of the fleet is, as the mapper of the key values adds them after the fleet's own steps. So the
     * contract number is taken out of them when the whole mapping is done, and is not kept as a key value as well.
     */
    default org.rutebanken.sobek.model.vehicle.Fleet mapToSobek(
            Fleet source,
            @Context MappingContext context
    ) {
        org.rutebanken.sobek.model.vehicle.Fleet fleet = mapFleetToSobek(source, context);
        if (fleet != null) {
            fleet.getKeyValues().stream()
                    .filter(keyValue -> CustomKeyValueTypes.CONTRACT_NUMBER_KEY.equals(keyValue.getKey()))
                    .findFirst()
                    .ifPresent(keyValue -> fleet.setContractNumber(keyValue.getValue()));
            fleet.removeKeyValue(CustomKeyValueTypes.CONTRACT_NUMBER_KEY);
        }
        return fleet;
    }

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

        // Any organisation reference is taken as it is, an OperatorRef or an AuthorityRef: Sobek keeps only the id
        if (source.getTransportOrganisationRef() != null && source.getTransportOrganisationRef().getValue() != null) {
            target.setTransportOrganisationRef(source.getTransportOrganisationRef().getValue().getRef());
        }

        context.getOwnedEntityMapper().updateSobekFromNetex(target, context);
    }

    @AfterMapping
    default void afterMapToNetex(
            org.rutebanken.sobek.model.vehicle.Fleet source,
            @MappingTarget Fleet target,
            @Context MappingContext context
    ) {
        // NeTEx can only write the organisation of a fleet as an OperatorRef or an AuthorityRef. It is always an
        // OperatorRef, whatever kind of organisation it is. It has no version: a reference with a version must be to an
        // Operator in the same delivery (the schema has a key reference for it), and Sobek does not export the organisations.
        if (source.getTransportOrganisationRef() != null) {
            target.withTransportOrganisationRef(OBJECT_FACTORY.createOperatorRef(
                    new OperatorRefStructure().withRef(source.getTransportOrganisationRef())));
        }

        // The key list of the fleet is completed with the other key values by the mapper of the key values
        if (source.getContractNumber() != null) {
            KeyValueStructure contractNumber = new KeyValueStructure()
                    .withKey(CustomKeyValueTypes.CONTRACT_NUMBER_KEY)
                    .withValue(source.getContractNumber());
            if (target.getKeyList() == null) {
                target.withKeyList(new KeyListStructure().withKeyValue(contractNumber));
            } else {
                target.getKeyList().withKeyValue(contractNumber);
            }
        }

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

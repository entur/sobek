package org.rutebanken.sobek.rest.netex;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.validation.ValidationException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;
import org.rutebanken.sobek.model.vehicle.AllPublicTransportModesEnumeration;
import org.rutebanken.sobek.model.vehicle.Fleet;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.model.vehicle.VehicleTypeKindEnumeration;
import org.rutebanken.sobek.repository.FleetRepository;
import org.rutebanken.sobek.repository.VehicleRepository;
import org.rutebanken.sobek.service.deactivate.FleetDeactivator;
import org.rutebanken.sobek.service.deactivate.VehicleDeactivator;
import org.rutebanken.sobek.service.deactivate.VehicleTypeDeactivator;
import org.rutebanken.sobek.versioning.VersionCreator;
import org.rutebanken.sobek.versioning.save.FleetVersionedSaverService;
import org.rutebanken.sobek.versioning.save.VehicleTypeVersionedSaverService;
import org.rutebanken.sobek.versioning.save.VehicleVersionedSaverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * What the register does to keep a fleet right when the things it refers to change: the members and the transport types
 * follow a new version of their vehicle or vehicle type, a new version of the fleet keeps them, and a vehicle or vehicle
 * type that a fleet is made of cannot be deactivated. The data is saved through the real savers, as the services do.
 */
@SpringBootTest
@Import(SingleOrganisationAuthorizationConfiguration.class)
class FleetReferencesIT {

    private static final String DATA_OWNER = SingleOrganisationAuthorizationConfiguration.DATA_OWNER;

    @Autowired private VehicleTypeVersionedSaverService vehicleTypeSaver;
    @Autowired private VehicleVersionedSaverService vehicleSaver;
    @Autowired private FleetVersionedSaverService fleetSaver;
    @Autowired private FleetRepository fleetRepository;
    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private VersionCreator versionCreator;
    @Autowired private VehicleDeactivator vehicleDeactivator;
    @Autowired private VehicleTypeDeactivator vehicleTypeDeactivator;
    @Autowired private FleetDeactivator fleetDeactivator;
    @Autowired private TransactionTemplate transactionTemplate;

    @Test
    @DisplayName("When a vehicle gets a new version, the fleets it is a member of follow it")
    void membersFollowANewVersionOfTheirVehicle() {
        VehicleType type = saveType("Follow type");
        Vehicle vehicle = saveVehicle("Follow vehicle", type);
        Fleet fleet = saveFleet("Follow fleet", List.of(vehicle), List.of());

        Vehicle newVersion = vehicleOf("Follow vehicle, changed", type);
        newVersion.setNetexId(vehicle.getNetexId());
        Vehicle saved = transactionTemplate.execute(status -> vehicleSaver.saveNewVersion(newVersion));

        assertThat(saved.getId()).isNotEqualTo(vehicle.getId());
        transactionTemplate.executeWithoutResult(status -> {
            Fleet current = fleetRepository.findFirstByNetexIdOrderByVersionDesc(fleet.getNetexId());
            assertThat(current.getVersion()).isEqualTo(fleet.getVersion());
            assertThat(current.getMembers()).extracting(Vehicle::getId).containsExactly(saved.getId());
        });
    }

    @Test
    @DisplayName("When a vehicle type gets a new version, the fleets that have it as a transport type follow it")
    void transportTypesFollowANewVersionOfTheirVehicleType() {
        VehicleType type = saveType("Follow transport type");
        Fleet fleet = saveFleet("Follow transport fleet", List.of(), List.of(type));

        VehicleType newVersion = typeOf("Follow transport type, changed");
        newVersion.setNetexId(type.getNetexId());
        VehicleType saved = transactionTemplate.execute(status -> vehicleTypeSaver.saveNewVersion(newVersion));

        assertThat(saved.getId()).isNotEqualTo(type.getId());
        transactionTemplate.executeWithoutResult(status -> {
            Fleet current = fleetRepository.findFirstByNetexIdOrderByVersionDesc(fleet.getNetexId());
            assertThat(current.getTransportTypes()).extracting(VehicleType::getId).containsExactly(saved.getId());
        });
    }

    @Test
    @DisplayName("A new version of a fleet has the same members and transport types, and the old version keeps its own")
    void newVersionOfAFleetKeepsItsMembers() {
        VehicleType type = saveType("Copy type");
        Vehicle vehicle = saveVehicle("Copy vehicle", type);
        Fleet fleet = saveFleet("Copy fleet", List.of(vehicle), List.of(type));

        Fleet copy = transactionTemplate.execute(status ->
                versionCreator.createCopy(fleetRepository.findFirstByNetexIdOrderByVersionDesc(fleet.getNetexId()), Fleet.class));
        copy.getName().setValue("Copy fleet, renamed");
        Fleet saved = transactionTemplate.execute(status -> fleetSaver.saveNewVersion(
                fleetRepository.findFirstByNetexIdOrderByVersionDesc(fleet.getNetexId()), copy, Instant.now()));

        assertThat(saved.getVersion()).isEqualTo(fleet.getVersion() + 1);
        transactionTemplate.executeWithoutResult(status -> {
            Fleet newest = fleetRepository.findFirstByNetexIdOrderByVersionDesc(fleet.getNetexId());
            assertThat(newest.getMembers()).extracting(Vehicle::getId).containsExactly(vehicle.getId());
            assertThat(newest.getTransportTypes()).extracting(VehicleType::getId).containsExactly(type.getId());
            // The copy is a version of its own: it does not share the rows of the join tables with the old one
            Fleet old = fleetRepository.findFirstByNetexIdAndVersion(fleet.getNetexId(), fleet.getVersion());
            assertThat(old.getMembers()).extracting(Vehicle::getId).containsExactly(vehicle.getId());
            assertThat(old.getId()).isNotEqualTo(newest.getId());
        });
    }

    @Test
    @DisplayName("A vehicle or a vehicle type that a fleet is made of cannot be deactivated, until the fleet is deactivated")
    void deactivationIsRefusedWhileAFleetUsesTheObject() throws Exception {
        // The transport type is one no vehicle uses, so that it is the fleet that keeps it from being deactivated
        VehicleType vehicleOfType = saveType("Deactivate vehicle type");
        VehicleType transportType = saveType("Deactivate transport type");
        Vehicle vehicle = saveVehicle("Deactivate vehicle", vehicleOfType);
        Fleet fleet = saveFleet("Deactivate fleet", List.of(vehicle), List.of(transportType));

        assertThatThrownBy(() -> vehicleDeactivator.deactivateVehicle(vehicle.getNetexId(), vehicle.getVersion(), Instant.now()))
                .isInstanceOf(ValidationException.class).hasMessageContaining("member of a fleet");
        assertThatThrownBy(() -> vehicleTypeDeactivator.deactivateVehicleType(transportType.getNetexId(), transportType.getVersion(), Instant.now()))
                .isInstanceOf(ValidationException.class).hasMessageContaining("transport type of a fleet");

        transactionTemplate.executeWithoutResult(status ->
                fleetDeactivator.deactivateFleet(fleet.getNetexId(), fleet.getVersion(), Instant.now()));
        waitForTheDatabaseClock();

        // The fleet is gone, so the vehicle can be deactivated
        transactionTemplate.executeWithoutResult(status ->
                vehicleDeactivator.deactivateVehicle(vehicle.getNetexId(), vehicle.getVersion(), Instant.now()));
        transactionTemplate.executeWithoutResult(status ->
                assertThat(vehicleRepository.findFirstByNetexIdOrderByVersionDesc(vehicle.getNetexId()).getVersion())
                        .isGreaterThan(vehicle.getVersion()));
    }

    /** The repository decides what has expired with the clock of the database, which in the test container is a little behind the application's. */
    private static void waitForTheDatabaseClock() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private VehicleType typeOf(String name) {
        VehicleType type = new VehicleType();
        type.setName(new EmbeddableMultilingualString(name, "en"));
        type.setTransportMode(AllPublicTransportModesEnumeration.BUS);
        type.setDataOwnerRef(DATA_OWNER);
        type.setVehicleTypeKind(VehicleTypeKindEnumeration.PHYSICAL);
        return type;
    }

    private VehicleType saveType(String name) {
        return vehicleTypeSaver.saveNewVersion(typeOf(name));
    }

    private Vehicle vehicleOf(String name, VehicleType type) {
        Vehicle vehicle = new Vehicle();
        vehicle.setName(new EmbeddableMultilingualString(name, "en"));
        vehicle.setTransportType(type);
        vehicle.setDataOwnerRef(DATA_OWNER);
        return vehicle;
    }

    private Vehicle saveVehicle(String name, VehicleType type) {
        return vehicleSaver.saveNewVersion(vehicleOf(name, type));
    }

    private Fleet saveFleet(String name, List<Vehicle> members, List<VehicleType> transportTypes) {
        Fleet fleet = new Fleet();
        fleet.setName(new EmbeddableMultilingualString(name, "en"));
        fleet.setDataOwnerRef(DATA_OWNER);
        fleet.getMembers().addAll(members);
        fleet.getTransportTypes().addAll(transportTypes);
        Fleet saved = fleetSaver.saveNewVersion(fleet);
        // The references of a fleet are to the rows of its members, so wait until the fleet is old enough to be found by the database clock
        waitForTheDatabaseClock();
        return saved;
    }
}

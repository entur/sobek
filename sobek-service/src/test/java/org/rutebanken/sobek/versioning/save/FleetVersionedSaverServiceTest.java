package org.rutebanken.sobek.versioning.save;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.validation.ValidationException;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rutebanken.sobek.model.ValidBetween;
import org.rutebanken.sobek.model.vehicle.Fleet;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.model.vehicle.VehicleTypeKindEnumeration;
import org.rutebanken.sobek.repository.FleetRepository;

@ExtendWith(MockitoExtension.class)
class FleetVersionedSaverServiceTest {

    @Mock private FleetRepository fleetRepository;
    @Mock private DefaultMergingVersionedSaverService defaultVersionedSaverService;
    @InjectMocks private FleetVersionedSaverService saver;

    @Test
    @DisplayName("A fleet of its owner's vehicles and physical vehicle types is saved")
    void validFleetIsSaved() {
        Fleet fleet = fleet("AKT", vehicle("AKT", 1L), type("AKT", 2L, VehicleTypeKindEnumeration.PHYSICAL));
        when(defaultVersionedSaverService.saveNewVersion(any(Fleet.class), any())).thenReturn(fleet);

        assertThat(saver.saveNewVersion(fleet)).isSameAs(fleet);
    }

    @Test
    @DisplayName("A fleet cannot have a member of another data owner")
    void memberOfAnotherOwnerIsRejected() {
        Fleet fleet = fleet("AKT", vehicle("RUT", 1L));

        assertThatThrownBy(() -> saver.saveNewVersion(fleet))
                .isInstanceOf(ValidationException.class).hasMessageContaining("RUT").hasMessageContaining("own data owner");
        verify(defaultVersionedSaverService, never()).saveNewVersion(any(Fleet.class), any());
    }

    @Test
    @DisplayName("A fleet cannot have a transport type of another data owner")
    void transportTypeOfAnotherOwnerIsRejected() {
        Fleet fleet = fleet("AKT", type("RUT", 2L, VehicleTypeKindEnumeration.PHYSICAL));

        assertThatThrownBy(() -> saver.saveNewVersion(fleet)).isInstanceOf(ValidationException.class).hasMessageContaining("RUT");
    }

    @Test
    @DisplayName("A fleet cannot have a contractual vehicle type")
    void contractualTransportTypeIsRejected() {
        Fleet fleet = fleet("AKT", type("AKT", 2L, VehicleTypeKindEnumeration.CONTRACTUAL));

        assertThatThrownBy(() -> saver.saveNewVersion(fleet)).isInstanceOf(ValidationException.class).hasMessageContaining("contractual");
    }

    @Test
    @DisplayName("A fleet cannot have a member or a transport type that has not been saved")
    void unsavedReferencesAreRejected() {
        assertThatThrownBy(() -> saver.saveNewVersion(fleet("AKT", vehicle("AKT", null)))).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> saver.saveNewVersion(fleet("AKT", type("AKT", null, VehicleTypeKindEnumeration.PHYSICAL))))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("A fleet cannot have a vehicle that has been deactivated")
    void deactivatedMemberIsRejected() {
        Vehicle deactivated = vehicle("AKT", 1L);
        deactivated.setValidBetween(new ValidBetween(Instant.now().minusSeconds(3600), Instant.now().minusSeconds(60)));

        assertThatThrownBy(() -> saver.saveNewVersion(fleet("AKT", deactivated)))
                .isInstanceOf(ValidationException.class).hasMessageContaining("deactivated");
    }

    private static Fleet fleet(String owner, Object... references) {
        Fleet fleet = new Fleet();
        fleet.setNetexId("AKT:Fleet:1");
        fleet.setDataOwnerRef(owner);
        for (Object reference : references) {
            if (reference instanceof Vehicle vehicle) {
                fleet.getMembers().add(vehicle);
            } else {
                fleet.getTransportTypes().add((VehicleType) reference);
            }
        }
        return fleet;
    }

    private static Vehicle vehicle(String owner, Long id) {
        Vehicle vehicle = new Vehicle();
        vehicle.setNetexId("AKT:Vehicle:" + id);
        vehicle.setDataOwnerRef(owner);
        vehicle.setId(id);
        return vehicle;
    }

    private static VehicleType type(String owner, Long id, VehicleTypeKindEnumeration kind) {
        VehicleType type = new VehicleType();
        type.setNetexId("AKT:VehicleType:" + id);
        type.setDataOwnerRef(owner);
        type.setVehicleTypeKind(kind);
        type.setId(id);
        return type;
    }
}

package org.rutebanken.sobek.versioning.save;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.model.vehicle.VehicleTypeKindEnumeration;
import org.rutebanken.sobek.repository.VehicleRepository;
import org.rutebanken.sobek.repository.VehicleTypeRepository;
import org.rutebanken.sobek.repository.listener.NetexIdAssigner;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleTypeKindRulesTest {

    @Mock
    private VehicleTypeRepository vehicleTypeRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private DefaultMergingVersionedSaverService defaultVersionedSaverService;
    @Mock
    private NetexIdAssigner netexIdAssigner;

    private VehicleTypeVersionedSaverService vehicleTypeSaver;
    private VehicleVersionedSaverService vehicleSaver;

    @BeforeEach
    void setUp() {
        vehicleTypeSaver = new VehicleTypeVersionedSaverService(vehicleTypeRepository, vehicleRepository, defaultVersionedSaverService, netexIdAssigner);
        vehicleSaver = new VehicleVersionedSaverService(vehicleRepository, defaultVersionedSaverService);
    }

    private VehicleType vehicleType(VehicleTypeKindEnumeration kind) {
        VehicleType vt = new VehicleType();
        vt.setNetexId(kind == VehicleTypeKindEnumeration.CONTRACTUAL ? "CVT:VehicleType:1" : "XXX:VehicleType:1");
        vt.setVehicleTypeKind(kind);
        return vt;
    }

    private VehicleType existingVehicleType(VehicleTypeKindEnumeration kind) {
        VehicleType vt = vehicleType(kind);
        vt.setId(1L);
        return vt;
    }

    /** Simulates the default saver persisting the new version as a new row (id 2). */
    private void stubSave() {
        when(defaultVersionedSaverService.saveNewVersion(any(), any(), any(Instant.class), any()))
                .thenAnswer(inv -> {
                    VehicleType saved = inv.getArgument(1);
                    saved.setId(2L);
                    return saved;
                });
    }

    @Test
    void newVehicleTypeWithoutKindIsRejected() {
        VehicleType incoming = vehicleType(null);
        incoming.setNetexId(null);
        assertThrows(IllegalArgumentException.class, () -> vehicleTypeSaver.saveNewVersion(null, incoming, Instant.now()));
        verify(defaultVersionedSaverService, never()).saveNewVersion(any(), any(), any(Instant.class), any());
    }

    @Test
    void newPhysicalVehicleTypeKeepsKind() {
        stubSave();
        VehicleType saved = vehicleTypeSaver.saveNewVersion(null, vehicleType(VehicleTypeKindEnumeration.PHYSICAL), Instant.now());
        assertEquals(VehicleTypeKindEnumeration.PHYSICAL, saved.getVehicleTypeKind());
    }

    @Test
    void newContractualVehicleTypeKeepsKind() {
        stubSave();
        VehicleType saved = vehicleTypeSaver.saveNewVersion(null, vehicleType(VehicleTypeKindEnumeration.CONTRACTUAL), Instant.now());
        assertEquals(VehicleTypeKindEnumeration.CONTRACTUAL, saved.getVehicleTypeKind());
    }

    @Test
    void updateWithoutKindInheritsFromExistingVersion() {
        stubSave();
        VehicleType existing = existingVehicleType(VehicleTypeKindEnumeration.CONTRACTUAL);
        VehicleType incoming = vehicleType(null);
        incoming.setNetexId(existing.getNetexId());
        VehicleType saved = vehicleTypeSaver.saveNewVersion(existing, incoming, Instant.now());
        assertEquals(VehicleTypeKindEnumeration.CONTRACTUAL, saved.getVehicleTypeKind());
    }

    @Test
    void updateWithSameKindIsAccepted() {
        stubSave();
        VehicleType existing = existingVehicleType(VehicleTypeKindEnumeration.PHYSICAL);
        VehicleType saved = vehicleTypeSaver.saveNewVersion(existing, vehicleType(VehicleTypeKindEnumeration.PHYSICAL), Instant.now());
        assertEquals(VehicleTypeKindEnumeration.PHYSICAL, saved.getVehicleTypeKind());
    }

    @Test
    void changingKindIsRejected() {
        VehicleType existing = existingVehicleType(VehicleTypeKindEnumeration.PHYSICAL);
        assertThrows(IllegalArgumentException.class,
                () -> vehicleTypeSaver.saveNewVersion(existing, vehicleType(VehicleTypeKindEnumeration.CONTRACTUAL), Instant.now()));
        verify(defaultVersionedSaverService, never()).saveNewVersion(any(), any(), any(Instant.class), any());
    }

    @Test
    void claimedContractualCodespaceIdOnPhysicalTypeIsRejected() {
        VehicleType incoming = vehicleType(VehicleTypeKindEnumeration.PHYSICAL);
        incoming.setNetexId("CVT:VehicleType:5");
        assertThrows(IllegalArgumentException.class, () -> vehicleTypeSaver.saveNewVersion(null, incoming, Instant.now()));
        verify(defaultVersionedSaverService, never()).saveNewVersion(any(), any(), any(Instant.class), any());
    }

    @Test
    void claimedOtherCodespaceIdOnContractualTypeIsRejected() {
        VehicleType incoming = vehicleType(VehicleTypeKindEnumeration.CONTRACTUAL);
        incoming.setNetexId("NMR:VehicleType:5");
        assertThrows(IllegalArgumentException.class, () -> vehicleTypeSaver.saveNewVersion(null, incoming, Instant.now()));
        verify(defaultVersionedSaverService, never()).saveNewVersion(any(), any(), any(Instant.class), any());
    }

    @Test
    void vehicleCannotReferenceContractualVehicleType() {
        Vehicle vehicle = new Vehicle();
        vehicle.setNetexId("XXX:Vehicle:1");
        vehicle.setTransportType(vehicleType(VehicleTypeKindEnumeration.CONTRACTUAL));

        assertThrows(IllegalArgumentException.class, () -> vehicleSaver.saveNewVersion(vehicle));
        assertThrows(IllegalArgumentException.class, () -> vehicleSaver.saveNewVersion(null, vehicle, Instant.now()));
        verify(defaultVersionedSaverService, never()).saveNewVersion(any(Vehicle.class), any());
    }

    @Test
    void vehicleCanReferencePhysicalVehicleType() {
        Vehicle vehicle = new Vehicle();
        vehicle.setTransportType(vehicleType(VehicleTypeKindEnumeration.PHYSICAL));
        when(defaultVersionedSaverService.saveNewVersion(any(Vehicle.class), any())).thenReturn(vehicle);

        assertEquals(vehicle, vehicleSaver.saveNewVersion(vehicle));
    }
}

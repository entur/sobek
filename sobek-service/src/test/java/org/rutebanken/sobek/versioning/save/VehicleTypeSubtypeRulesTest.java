package org.rutebanken.sobek.versioning.save;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rutebanken.sobek.error.VehicleTypeKindViolationException;
import org.rutebanken.sobek.model.vehicle.CompoundTrain;
import org.rutebanken.sobek.model.vehicle.Train;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.model.vehicle.VehicleTypeKindEnumeration;
import org.rutebanken.sobek.netex.id.ValidPrefixList;
import org.rutebanken.sobek.repository.VehicleRepository;
import org.rutebanken.sobek.repository.VehicleTypeRepository;
import org.rutebanken.sobek.repository.listener.NetexIdAssigner;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleTypeSubtypeRulesTest {

    @Mock
    private VehicleTypeRepository vehicleTypeRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private DefaultMergingVersionedSaverService defaultVersionedSaverService;
    @Mock
    private NetexIdAssigner netexIdAssigner;

    private VehicleTypeVersionedSaverService saver;

    @BeforeEach
    void setUp() {
        saver = new VehicleTypeVersionedSaverService(vehicleTypeRepository, vehicleRepository, defaultVersionedSaverService, netexIdAssigner,
                new ValidPrefixList("NMR", List.of("RUT")));
    }

    private static <T extends VehicleType> T vehicleType(T vt, long id) {
        vt.setId(id);
        vt.setNetexId("XXX:VehicleType:1");
        vt.setVehicleTypeKind(VehicleTypeKindEnumeration.PHYSICAL);
        return vt;
    }

    private void stubSave() {
        when(defaultVersionedSaverService.saveNewVersion(any(), any(), any(Instant.class), any()))
                .thenAnswer(inv -> {
                    VehicleType saved = inv.getArgument(1);
                    saved.setId(2L);
                    return saved;
                });
    }

    @Test
    void newVersionOfAnotherSubtypeIsRejected() {
        VehicleType existing = vehicleType(new Train(), 1L);
        VehicleType incoming = vehicleType(new CompoundTrain(), 0L);

        ValidationException e = assertThrows(ValidationException.class,
                () -> saver.saveNewVersion(existing, incoming, Instant.now()));

        assertThat(e.getMessage()).contains("Train").contains("CompoundTrain");
        verify(defaultVersionedSaverService, never()).saveNewVersion(any(), any(), any(Instant.class), any());
    }

    @Test
    void plainVehicleTypeCannotBecomeATrain() {
        VehicleType existing = vehicleType(new VehicleType(), 1L);
        VehicleType incoming = vehicleType(new Train(), 0L);

        assertThrows(ValidationException.class, () -> saver.saveNewVersion(existing, incoming, Instant.now()));
    }

    @Test
    void contractualTrainMustBeInTheContractualCodespaceAndPhysicalTrainMustNot() {
        VehicleType contractualInPhysicalCodespace = vehicleType(new Train(), 0L);
        contractualInPhysicalCodespace.setNetexId("NMR:Train:1");
        contractualInPhysicalCodespace.setVehicleTypeKind(VehicleTypeKindEnumeration.CONTRACTUAL);
        VehicleType physicalInContractualCodespace = vehicleType(new CompoundTrain(), 0L);
        physicalInContractualCodespace.setNetexId("CVT:CompoundTrain:1");

        assertThrows(VehicleTypeKindViolationException.class,
                () -> saver.saveNewVersion(null, contractualInPhysicalCodespace, Instant.now()));
        assertThrows(VehicleTypeKindViolationException.class,
                () -> saver.saveNewVersion(null, physicalInContractualCodespace, Instant.now()));
    }

    @Test
    void contractualTrainInTheContractualCodespaceIsAccepted() {
        stubSave();
        VehicleType train = vehicleType(new Train(), 0L);
        train.setNetexId("CVT:Train:1");
        train.setVehicleTypeKind(VehicleTypeKindEnumeration.CONTRACTUAL);

        saver.saveNewVersion(null, train, Instant.now());

        verify(defaultVersionedSaverService).saveNewVersion(any(), any(), any(Instant.class), any());
    }

    @Test
    void newVersionOfSameSubtypeMovesComponentReferencesToTheNewVersion() {
        stubSave();
        VehicleType existing = vehicleType(new Train(), 1L);
        VehicleType incoming = vehicleType(new Train(), 0L);

        saver.saveNewVersion(existing, incoming, Instant.now());

        verify(vehicleRepository).moveToTransportType(1L, 2L);
        verify(vehicleTypeRepository).moveComponentReferences(1L, 2L);
    }
}

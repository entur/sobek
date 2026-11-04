package org.rutebanken.sobek.versioning.save;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rutebanken.sobek.auth.AuthorizationService;
import org.rutebanken.sobek.auth.UsernameFetcher;
import org.rutebanken.sobek.diff.SobekObjectDiffer;
import org.rutebanken.sobek.model.KeyValue;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.netex.id.ValidPrefixList;
import org.rutebanken.sobek.organisation.OrganisationRegistry;
import org.rutebanken.sobek.repository.EntityInVersionRepository;
import org.rutebanken.sobek.repository.listener.NetexIdAssigner;
import org.rutebanken.sobek.versioning.ValidityUpdater;
import org.rutebanken.sobek.versioning.VersionIncrementor;
import org.rutebanken.sobek.versioning.validate.VersionValidator;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * What the saver does with the id of a new object, by the codespace the id is in:
 * Sobek's (NMR, CVT): rejected unless the object exists, which makes it an update.
 * Registered: the client's id is used. External: Sobek assigns an id and keeps the external one as imported-id.
 */
@ExtendWith(MockitoExtension.class)
class NewObjectWithClientIdTest {

    @Mock private UsernameFetcher usernameFetcher;
    @Mock private ValidityUpdater validityUpdater;
    @Mock private SobekObjectDiffer sobekObjectDiffer;
    @Mock private VersionIncrementor versionIncrementor;
    @Mock private NetexIdAssigner netexIdAssigner;
    @Mock private AuthorizationService authorizationService;
    @Mock private VersionValidator versionValidator;
    @Mock private OrganisationRegistry organisationRegistry;
    @Mock private EntityInVersionRepository<Vehicle> vehicleRepository;
    @Mock private EntityInVersionRepository<VehicleType> vehicleTypeRepository;

    private DefaultMergingVersionedSaverService saver;

    @BeforeEach
    void setUp() {
        saver = new DefaultMergingVersionedSaverService(usernameFetcher, validityUpdater, sobekObjectDiffer, versionIncrementor,
                netexIdAssigner, authorizationService, versionValidator, organisationRegistry,
                new ValidPrefixList("NMR", List.of("RUT", "AKT")));
        org.mockito.Mockito.lenient().when(validityUpdater.updateValidBetween(any(), any(), any())).thenReturn(Instant.now());
        org.mockito.Mockito.lenient().when(vehicleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        org.mockito.Mockito.lenient().when(vehicleTypeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private static Vehicle vehicle(String netexId) {
        Vehicle vehicle = new Vehicle();
        vehicle.setNetexId(netexId);
        return vehicle;
    }

    private void save(Vehicle vehicle) {
        saver.saveNewVersion(null, vehicle, Instant.now(), vehicleRepository);
    }

    // --- Sobek's own codespaces ---

    @Test
    void newObjectWithAnIdInSobeksCodespaceIsRejected() {
        when(vehicleRepository.findFirstByNetexIdOrderByVersionDesc("NMR:Vehicle:5")).thenReturn(null);

        assertThatThrownBy(() -> save(vehicle("NMR:Vehicle:5")))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("NMR:Vehicle:5")
                .hasMessageContaining("assigned by Sobek");
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void newVehicleTypeWithAnIdInTheContractualCodespaceIsRejected() {
        VehicleType type = new VehicleType();
        type.setNetexId("CVT:VehicleType:5");
        when(vehicleTypeRepository.findFirstByNetexIdOrderByVersionDesc("CVT:VehicleType:5")).thenReturn(null);

        assertThatThrownBy(() -> saver.saveNewVersion(null, type, Instant.now(), vehicleTypeRepository))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("CVT:VehicleType:5")
                .hasMessageContaining("assigned by Sobek");
        verify(vehicleTypeRepository, never()).save(any());
    }

    @Test
    void anIdInSobeksCodespaceThatExistsIsAnUpdate() {
        Vehicle existing = vehicle("NMR:Vehicle:5");
        existing.setVersion(1L);
        when(vehicleRepository.findFirstByNetexIdOrderByVersionDesc("NMR:Vehicle:5")).thenReturn(existing);

        Vehicle update = vehicle("NMR:Vehicle:5");
        save(update);

        // The new version is saved as a version of the existing object, with the id kept, and nothing is assigned
        verify(vehicleRepository).save(update);
        assertThat(update.getNetexId()).isEqualTo("NMR:Vehicle:5");
        assertThat(update.getVersion()).isEqualTo(existing.getVersion());
        verify(netexIdAssigner, never()).assignNetexId(any());
    }

    // --- registered codespaces ---

    @Test
    void newObjectWithAnIdInARegisteredCodespaceKeepsThatId() {
        when(vehicleRepository.findFirstByNetexIdOrderByVersionDesc("RUT:Vehicle:5")).thenReturn(null);

        Vehicle vehicle = vehicle("RUT:Vehicle:5");
        save(vehicle);

        verify(vehicleRepository).save(vehicle);
        assertThat(vehicle.getNetexId()).isEqualTo("RUT:Vehicle:5");
        assertThat(vehicle.getKeyValues()).isEmpty();
        verify(netexIdAssigner, never()).assignNetexId(any());
    }

    // --- external codespaces ---

    @Test
    void newObjectWithAnExternalIdGetsAnIdOfItsOwnAndKeepsTheExternalOneAsImportedId() {
        when(vehicleRepository.findFirstByNetexIdOrderByVersionDesc("AUTOSYS:Vehicle:77")).thenReturn(null);

        Vehicle vehicle = vehicle("AUTOSYS:Vehicle:77");
        save(vehicle);

        assertThat(vehicle.getKeyValues()).extracting(KeyValue::getKey, KeyValue::getValue)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("imported-id", "AUTOSYS:Vehicle:77"));
        // The external id is no longer the id; the assigner gives the object the id of Sobek's codespace
        assertThat(vehicle.getNetexId()).isNull();
        verify(netexIdAssigner).assignNetexId(vehicle);
        verify(vehicleRepository).save(vehicle);
    }

    @Test
    void externalIdReplacesAnImportedIdThatWasAlreadyOnTheObject() {
        when(vehicleRepository.findFirstByNetexIdOrderByVersionDesc("AUTOSYS:Vehicle:77")).thenReturn(null);

        Vehicle vehicle = vehicle("AUTOSYS:Vehicle:77");
        vehicle.addKeyValue("imported-id", "OLD:Vehicle:1");
        save(vehicle);

        assertThat(vehicle.getKeyValues()).extracting(KeyValue::getValue).containsExactly("AUTOSYS:Vehicle:77");
    }

    @Test
    void idThatIsNotOnTheNetexFormIsTreatedAsExternal() {
        when(vehicleRepository.findFirstByNetexIdOrderByVersionDesc("just-some-text")).thenReturn(null);

        Vehicle vehicle = vehicle("just-some-text");
        save(vehicle);

        assertThat(vehicle.getKeyValues()).extracting(KeyValue::getValue).containsExactly("just-some-text");
        verify(netexIdAssigner).assignNetexId(vehicle);
    }

    // --- no id ---

    @Test
    void newObjectWithoutAnIdGetsOneAssigned() {
        Vehicle vehicle = vehicle(null);
        save(vehicle);

        verify(netexIdAssigner).assignNetexId(vehicle);
        assertThat(vehicle.getKeyValues()).isEmpty();
    }
}

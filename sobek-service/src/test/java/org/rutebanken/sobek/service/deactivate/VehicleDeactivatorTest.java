package org.rutebanken.sobek.service.deactivate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rutebanken.sobek.auth.AuthorizationService;
import org.rutebanken.sobek.auth.UsernameFetcher;
import org.rutebanken.sobek.model.ValidBetween;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.repository.VehicleRepository;
import org.rutebanken.sobek.versioning.VersionCreator;
import org.rutebanken.sobek.versioning.save.VehicleVersionedSaverService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleDeactivatorTest {

    @Mock
    private VehicleVersionedSaverService vehicleVersionedSaverService;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private UsernameFetcher usernameFetcher;

    @Mock
    private VersionCreator versionCreator;

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private VehicleDeactivator vehicleDeactivator;

    private static final String NETEX_ID = "VEH:001";
    private static final Long VERSION = 1L;
    private static final String USERNAME = "testuser";

    private Vehicle previousVersion;
    private Vehicle nextVersion;
    private Instant today;
    private Instant futureDate;

    @BeforeEach
    void setUp() {
        today = Instant.now().truncatedTo(ChronoUnit.DAYS);
        futureDate = today.plus(30, ChronoUnit.DAYS);

        previousVersion = createVehicle(NETEX_ID, VERSION, today.minus(365, ChronoUnit.DAYS), null);
        nextVersion = createVehicle(NETEX_ID, VERSION + 1, today, futureDate);

        when(usernameFetcher.getUserNameForAuthenticatedUser()).thenReturn(USERNAME);
    }

    @Test
    void deactivateVehicle_Success() {
        // Given
        when(vehicleRepository.findFirstByNetexIdOrderByVersionDesc(NETEX_ID)).thenReturn(previousVersion);
        when(authorizationService.canDeleteEntity(previousVersion)).thenReturn(true);
        when(versionCreator.createCopy(previousVersion, Vehicle.class)).thenReturn(nextVersion);
        when(vehicleVersionedSaverService.saveNewVersion(eq(previousVersion), eq(nextVersion), eq(today)))
                .thenReturn(nextVersion);

        // When
        Vehicle result = vehicleDeactivator.deactivateVehicle(NETEX_ID, VERSION, futureDate);

        // Then
        assertNotNull(result);
        assertEquals(NETEX_ID, result.getNetexId());
        assertEquals(today, previousVersion.getValidBetween().getToDate());
        assertEquals(today, nextVersion.getValidBetween().getFromDate());
        assertEquals(futureDate, nextVersion.getValidBetween().getToDate());

        verify(usernameFetcher).getUserNameForAuthenticatedUser();
        verify(vehicleRepository).findFirstByNetexIdOrderByVersionDesc(NETEX_ID);
        verify(authorizationService).canDeleteEntity(previousVersion);
        verify(versionCreator).createCopy(previousVersion, Vehicle.class);
        verify(vehicleVersionedSaverService).saveNewVersion(previousVersion, nextVersion, today);
    }

    @Test
    void deactivateVehicle_DeactivationDateInPast_UsesToday() {
        // Given
        Instant pastDate = today.minus(10, ChronoUnit.DAYS);
        nextVersion.setValidBetween(new ValidBetween(today, today));

        when(vehicleRepository.findFirstByNetexIdOrderByVersionDesc(NETEX_ID)).thenReturn(previousVersion);
        when(authorizationService.canDeleteEntity(previousVersion)).thenReturn(true);
        when(versionCreator.createCopy(previousVersion, Vehicle.class)).thenReturn(nextVersion);
        when(vehicleVersionedSaverService.saveNewVersion(eq(previousVersion), eq(nextVersion), eq(today)))
                .thenReturn(nextVersion);

        // When
        Vehicle result = vehicleDeactivator.deactivateVehicle(NETEX_ID, VERSION, pastDate);

        // Then
        assertNotNull(result);
        assertEquals(today, nextVersion.getValidBetween().getToDate());
        verify(vehicleVersionedSaverService).saveNewVersion(previousVersion, nextVersion, today);
    }

    @Test
    void deactivateVehicle_VehicleNotFound_ThrowsException() {
        // Given
        when(vehicleRepository.findFirstByNetexIdOrderByVersionDesc(NETEX_ID)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                vehicleDeactivator.deactivateVehicle(NETEX_ID, VERSION, futureDate)
        );

        assertEquals("Cannot find vehicle to deactivate: " + NETEX_ID + ". No changes executed.", exception.getMessage());
        verify(vehicleRepository).findFirstByNetexIdOrderByVersionDesc(NETEX_ID);
        verify(authorizationService, never()).canDeleteEntity(any());
        verify(versionCreator, never()).createCopy(any(), any());
        verify(vehicleVersionedSaverService, never()).saveNewVersion(any(), any(), any());
    }

    @Test
    void deactivateVehicle_UnauthorizedUser_ThrowsException() {
        // Given
        when(vehicleRepository.findFirstByNetexIdOrderByVersionDesc(NETEX_ID)).thenReturn(previousVersion);
        when(authorizationService.canDeleteEntity(previousVersion)).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                vehicleDeactivator.deactivateVehicle(NETEX_ID, VERSION, futureDate)
        );

        assertEquals("User is not authorized to deactivate vehicle " + NETEX_ID, exception.getMessage());
        verify(authorizationService).canDeleteEntity(previousVersion);
        verify(versionCreator, never()).createCopy(any(), any());
        verify(vehicleVersionedSaverService, never()).saveNewVersion(any(), any(), any());
    }

    @Test
    void deactivateVehicle_VersionMismatch_ThrowsException() {
        // Given
        previousVersion.setVersion(5L);
        when(vehicleRepository.findFirstByNetexIdOrderByVersionDesc(NETEX_ID)).thenReturn(previousVersion);
        when(authorizationService.canDeleteEntity(previousVersion)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                vehicleDeactivator.deactivateVehicle(NETEX_ID, VERSION, futureDate)
        );

        assertEquals("The vehicle " + NETEX_ID + " has a different version than expected. Expected version: " + VERSION + ", actual version: " + previousVersion.getVersion(),
                exception.getMessage());
        verify(vehicleRepository).findFirstByNetexIdOrderByVersionDesc(NETEX_ID);
        verify(authorizationService).canDeleteEntity(previousVersion);
        verify(versionCreator, never()).createCopy(any(), any());
        verify(vehicleVersionedSaverService, never()).saveNewVersion(any(), any(), any());
    }

    @Test
    void deactivateVehicle_AlreadyDeactivated_ThrowsException() {
        // Given
        Instant existingDeactivationDate = today.minus(10, ChronoUnit.DAYS);
        previousVersion.setValidBetween(new ValidBetween(today.minus(365, ChronoUnit.DAYS), existingDeactivationDate));

        when(vehicleRepository.findFirstByNetexIdOrderByVersionDesc(NETEX_ID)).thenReturn(previousVersion);
        when(authorizationService.canDeleteEntity(previousVersion)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                vehicleDeactivator.deactivateVehicle(NETEX_ID, VERSION, futureDate)
        );

        assertEquals("The vehicle " + NETEX_ID + ", version " + VERSION + " is already deactivated at " + existingDeactivationDate,
                exception.getMessage());
        verify(authorizationService).canDeleteEntity(previousVersion);
        verify(versionCreator, never()).createCopy(any(), any());
        verify(vehicleVersionedSaverService, never()).saveNewVersion(any(), any(), any());
    }

    @Test
    void deactivateVehicle_DeactivateToday_Success() {
        // Given
        nextVersion.setValidBetween(new ValidBetween(today, today));

        when(vehicleRepository.findFirstByNetexIdOrderByVersionDesc(NETEX_ID)).thenReturn(previousVersion);
        when(authorizationService.canDeleteEntity(previousVersion)).thenReturn(true);
        when(versionCreator.createCopy(previousVersion, Vehicle.class)).thenReturn(nextVersion);
        when(vehicleVersionedSaverService.saveNewVersion(eq(previousVersion), eq(nextVersion), eq(today)))
                .thenReturn(nextVersion);

        // When
        Vehicle result = vehicleDeactivator.deactivateVehicle(NETEX_ID, VERSION, today);

        // Then
        assertNotNull(result);
        assertEquals(today, nextVersion.getValidBetween().getToDate());
        verify(vehicleVersionedSaverService).saveNewVersion(previousVersion, nextVersion, today);
    }

    @Test
    void deactivateVehicle_ValidBetweenNotNull_Success() {
        // Given
        String netexId = "VEH:007";
        Long expectedVersion = 10L;

        Vehicle prevVersion = createVehicle(netexId, 10L, today.minus(365, ChronoUnit.DAYS), null);
        Vehicle nextVer = createVehicle(netexId, 11L, today, futureDate);

        when(vehicleRepository.findFirstByNetexIdOrderByVersionDesc(netexId)).thenReturn(prevVersion);
        when(authorizationService.canDeleteEntity(prevVersion)).thenReturn(true);
        when(versionCreator.createCopy(prevVersion, Vehicle.class)).thenReturn(nextVer);
        when(vehicleVersionedSaverService.saveNewVersion(eq(prevVersion), eq(nextVer), eq(today)))
                .thenReturn(nextVer);

        // When
        Vehicle result = vehicleDeactivator.deactivateVehicle(netexId, expectedVersion, futureDate);

        // Then
        assertNotNull(result);
        verify(vehicleVersionedSaverService).saveNewVersion(prevVersion, nextVer, today);
    }

    private Vehicle createVehicle(String netexId, Long version, Instant fromDate, Instant toDate) {
        Vehicle vehicle = new Vehicle();
        vehicle.setNetexId(netexId);
        vehicle.setVersion(version);
        vehicle.setValidBetween(new ValidBetween(fromDate, toDate));
        return vehicle;
    }
}
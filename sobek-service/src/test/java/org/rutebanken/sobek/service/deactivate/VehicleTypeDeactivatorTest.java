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
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.repository.VehicleTypeRepository;
import org.rutebanken.sobek.versioning.VersionCreator;
import org.rutebanken.sobek.versioning.save.VehicleTypeVersionedSaverService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleTypeDeactivatorTest {

    @Mock
    private VehicleTypeVersionedSaverService vehicleTypeVersionedSaverService;

    @Mock
    private VehicleTypeRepository vehicleTypeRepository;

    @Mock
    private UsernameFetcher usernameFetcher;

    @Mock
    private VersionCreator versionCreator;

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private VehicleTypeDeactivator vehicleTypeDeactivator;

    private static final String NETEX_ID = "VT:001";
    private static final Long VERSION = 1L;
    private static final String USERNAME = "testuser";

    private VehicleType previousVersion;
    private VehicleType nextVersion;
    private Instant today;
    private Instant futureDate;

    @BeforeEach
    void setUp() {
        today = Instant.now().truncatedTo(ChronoUnit.DAYS);
        futureDate = today.plus(30, ChronoUnit.DAYS);

        previousVersion = createVehicleType(NETEX_ID, VERSION, today.minus(365, ChronoUnit.DAYS), null);
        nextVersion = createVehicleType(NETEX_ID, VERSION + 1, today, futureDate);

        when(usernameFetcher.getUserNameForAuthenticatedUser()).thenReturn(USERNAME);
    }

    @Test
    void deactivateVehicleType_Success() {
        // Given
        when(vehicleTypeRepository.findFirstByNetexIdOrderByVersionDesc(NETEX_ID)).thenReturn(previousVersion);
        when(authorizationService.canDeleteEntity(previousVersion)).thenReturn(true);
        when(versionCreator.createCopy(previousVersion, VehicleType.class)).thenReturn(nextVersion);
        when(vehicleTypeVersionedSaverService.saveNewVersion(eq(previousVersion), eq(nextVersion), eq(today)))
                .thenReturn(nextVersion);

        // When
        VehicleType result = vehicleTypeDeactivator.deactivateVehicleType(NETEX_ID, VERSION, futureDate);

        // Then
        assertNotNull(result);
        assertEquals(NETEX_ID, result.getNetexId());
        assertEquals(today, previousVersion.getValidBetween().getToDate());
        assertEquals(today, nextVersion.getValidBetween().getFromDate());
        assertEquals(futureDate, nextVersion.getValidBetween().getToDate());

        verify(usernameFetcher).getUserNameForAuthenticatedUser();
        verify(vehicleTypeRepository).findFirstByNetexIdOrderByVersionDesc(NETEX_ID);
        verify(authorizationService).canDeleteEntity(previousVersion);
        verify(versionCreator).createCopy(previousVersion, VehicleType.class);
        verify(vehicleTypeVersionedSaverService).saveNewVersion(previousVersion, nextVersion, today);
    }

    @Test
    void deactivateVehicleType_DeactivationDateInPast_UsesToday() {
        // Given
        Instant pastDate = today.minus(10, ChronoUnit.DAYS);
        nextVersion.setValidBetween(new ValidBetween(today, today));

        when(vehicleTypeRepository.findFirstByNetexIdOrderByVersionDesc(NETEX_ID)).thenReturn(previousVersion);
        when(authorizationService.canDeleteEntity(previousVersion)).thenReturn(true);
        when(versionCreator.createCopy(previousVersion, VehicleType.class)).thenReturn(nextVersion);
        when(vehicleTypeVersionedSaverService.saveNewVersion(eq(previousVersion), eq(nextVersion), eq(today)))
                .thenReturn(nextVersion);

        // When
        VehicleType result = vehicleTypeDeactivator.deactivateVehicleType(NETEX_ID, VERSION, pastDate);

        // Then
        assertNotNull(result);
        assertEquals(today, nextVersion.getValidBetween().getToDate());
        verify(vehicleTypeVersionedSaverService).saveNewVersion(previousVersion, nextVersion, today);
    }

    @Test
    void deactivateVehicleType_VehicleTypeNotFound_ThrowsException() {
        // Given
        when(vehicleTypeRepository.findFirstByNetexIdOrderByVersionDesc(NETEX_ID)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                vehicleTypeDeactivator.deactivateVehicleType(NETEX_ID, VERSION, futureDate)
        );

        assertEquals("Cannot find vehicle type to deactivate: " + NETEX_ID + ". No changes executed.", exception.getMessage());
        verify(vehicleTypeRepository).findFirstByNetexIdOrderByVersionDesc(NETEX_ID);
        verify(authorizationService, never()).canDeleteEntity(any());
        verify(versionCreator, never()).createCopy(any(), any());
        verify(vehicleTypeVersionedSaverService, never()).saveNewVersion(any(), any(), any());
    }

    @Test
    void deactivateVehicleType_UnauthorizedUser_ThrowsException() {
        // Given
        when(vehicleTypeRepository.findFirstByNetexIdOrderByVersionDesc(NETEX_ID)).thenReturn(previousVersion);
        when(authorizationService.canDeleteEntity(previousVersion)).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                vehicleTypeDeactivator.deactivateVehicleType(NETEX_ID, VERSION, futureDate)
        );

        assertEquals("User is not authorized to deactivate vehicle type " + NETEX_ID, exception.getMessage());
        verify(authorizationService).canDeleteEntity(previousVersion);
        verify(versionCreator, never()).createCopy(any(), any());
        verify(vehicleTypeVersionedSaverService, never()).saveNewVersion(any(), any(), any());
    }

    @Test
    void deactivateVehicleType_VersionMismatch_ThrowsException() {
        // Given
        previousVersion.setVersion(5L);
        when(vehicleTypeRepository.findFirstByNetexIdOrderByVersionDesc(NETEX_ID)).thenReturn(previousVersion);
        when(authorizationService.canDeleteEntity(previousVersion)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                vehicleTypeDeactivator.deactivateVehicleType(NETEX_ID, VERSION, futureDate)
        );

        assertEquals("The vehicle type " + NETEX_ID + " has a different version than expected. Expected version: " + VERSION + ", actual version: " + previousVersion.getVersion(),
                exception.getMessage());
        verify(vehicleTypeRepository).findFirstByNetexIdOrderByVersionDesc(NETEX_ID);
        verify(authorizationService).canDeleteEntity(previousVersion);
        verify(versionCreator, never()).createCopy(any(), any());
        verify(vehicleTypeVersionedSaverService, never()).saveNewVersion(any(), any(), any());
    }

    @Test
    void deactivateVehicleType_AlreadyDeactivated_ThrowsException() {
        // Given
        Instant existingDeactivationDate = today.minus(10, ChronoUnit.DAYS);
        previousVersion.setValidBetween(new ValidBetween(today.minus(365, ChronoUnit.DAYS), existingDeactivationDate));

        when(vehicleTypeRepository.findFirstByNetexIdOrderByVersionDesc(NETEX_ID)).thenReturn(previousVersion);
        when(authorizationService.canDeleteEntity(previousVersion)).thenReturn(true);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                vehicleTypeDeactivator.deactivateVehicleType(NETEX_ID, VERSION, futureDate)
        );

        assertEquals("The vehicle type " + NETEX_ID + ", version " + VERSION + " is already deactivated at " + existingDeactivationDate,
                exception.getMessage());
        verify(authorizationService).canDeleteEntity(previousVersion);
        verify(versionCreator, never()).createCopy(any(), any());
        verify(vehicleTypeVersionedSaverService, never()).saveNewVersion(any(), any(), any());
    }

    @Test
    void deactivateVehicleType_DeactivateToday_Success() {
        // Given
        nextVersion.setValidBetween(new ValidBetween(today, today));

        when(vehicleTypeRepository.findFirstByNetexIdOrderByVersionDesc(NETEX_ID)).thenReturn(previousVersion);
        when(authorizationService.canDeleteEntity(previousVersion)).thenReturn(true);
        when(versionCreator.createCopy(previousVersion, VehicleType.class)).thenReturn(nextVersion);
        when(vehicleTypeVersionedSaverService.saveNewVersion(eq(previousVersion), eq(nextVersion), eq(today)))
                .thenReturn(nextVersion);

        // When
        VehicleType result = vehicleTypeDeactivator.deactivateVehicleType(NETEX_ID, VERSION, today);

        // Then
        assertNotNull(result);
        assertEquals(today, nextVersion.getValidBetween().getToDate());
        verify(vehicleTypeVersionedSaverService).saveNewVersion(previousVersion, nextVersion, today);
    }

    @Test
    void deactivateVehicleType_ValidBetweenNotNull_Success() {
        // Given
        String netexId = "VT:007";
        Long expectedVersion = 10L;

        VehicleType prevVersion = createVehicleType(netexId, 10L, today.minus(365, ChronoUnit.DAYS), null);
        VehicleType nextVer = createVehicleType(netexId, 11L, today, futureDate);

        when(vehicleTypeRepository.findFirstByNetexIdOrderByVersionDesc(netexId)).thenReturn(prevVersion);
        when(authorizationService.canDeleteEntity(prevVersion)).thenReturn(true);
        when(versionCreator.createCopy(prevVersion, VehicleType.class)).thenReturn(nextVer);
        when(vehicleTypeVersionedSaverService.saveNewVersion(eq(prevVersion), eq(nextVer), eq(today)))
                .thenReturn(nextVer);

        // When
        VehicleType result = vehicleTypeDeactivator.deactivateVehicleType(netexId, expectedVersion, futureDate);

        // Then
        assertNotNull(result);
        verify(vehicleTypeVersionedSaverService).saveNewVersion(prevVersion, nextVer, today);
    }

    private VehicleType createVehicleType(String netexId, Long version, Instant fromDate, Instant toDate) {
        VehicleType vehicleType = new VehicleType();
        vehicleType.setNetexId(netexId);
        vehicleType.setVersion(version);
        vehicleType.setValidBetween(new ValidBetween(fromDate, toDate));
        return vehicleType;
    }
}
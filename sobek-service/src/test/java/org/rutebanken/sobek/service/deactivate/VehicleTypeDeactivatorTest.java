package org.rutebanken.sobek.service.deactivate;

import jakarta.xml.bind.ValidationException;
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
import org.springframework.security.access.AccessDeniedException;

import java.time.Duration;
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
    private Instant now;
    private Instant futureDate;

    @BeforeEach
    void setUp() {
        now = Instant.now();
        futureDate = now.plus(30, ChronoUnit.DAYS);

        previousVersion = createVehicleType(NETEX_ID, VERSION, now.minus(365, ChronoUnit.DAYS), null);
        nextVersion = createVehicleType(NETEX_ID, VERSION + 1, now, futureDate);

        lenient().when(usernameFetcher.getUserNameForAuthenticatedUser()).thenReturn(USERNAME);
    }

    @Test
    void deactivateVehicleType_Success() throws ValidationException {
        // Given
        when(vehicleTypeRepository.findFirstByNetexIdOrderByVersionDesc(NETEX_ID)).thenReturn(previousVersion);
        when(authorizationService.canDeleteEntity(previousVersion)).thenReturn(true);
        when(versionCreator.createCopy(previousVersion, VehicleType.class)).thenReturn(nextVersion);
        when(vehicleTypeVersionedSaverService.saveNewVersion(eq(previousVersion), eq(nextVersion), any()))
                .thenReturn(nextVersion);

        // When
        VehicleType result = vehicleTypeDeactivator.deactivateVehicleType(NETEX_ID, VERSION, futureDate);

        // Then
        assertNotNull(result);
        assertEquals(NETEX_ID, result.getNetexId());
        assertInstantCloseTo(now, previousVersion.getValidBetween().getToDate(), 60);
        assertInstantCloseTo(now, nextVersion.getValidBetween().getFromDate(), 60);
        assertInstantCloseTo(futureDate, nextVersion.getValidBetween().getToDate(), 60);

        verify(usernameFetcher).getUserNameForAuthenticatedUser();
        verify(vehicleTypeRepository).findFirstByNetexIdOrderByVersionDesc(NETEX_ID);
        verify(authorizationService).canDeleteEntity(previousVersion);
        verify(versionCreator).createCopy(previousVersion, VehicleType.class);
        verify(vehicleTypeVersionedSaverService).saveNewVersion(eq(previousVersion), eq(nextVersion), any(Instant.class));
    }

    @Test
    void deactivateVehicleType_DeactivationDateInPast_ThrowsException() {
        // Given
        Instant pastDate = now.minus(10, ChronoUnit.DAYS);

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () ->
                vehicleTypeDeactivator.deactivateVehicleType(NETEX_ID, VERSION, pastDate)
        );

        // Then
        assertTrue(exception.getMessage().contains("cannot be set backwards in time") ||
                exception.getMessage().contains("cannot be in the past"));

        verify(versionCreator, never()).createCopy(any(), any());
        verify(vehicleTypeVersionedSaverService, never()).saveNewVersion(any(), any(), any());
    }

    @Test
    void deactivateVehicleType_VehicleTypeNotFound_ThrowsException() {
        // Given
        when(vehicleTypeRepository.findFirstByNetexIdOrderByVersionDesc(NETEX_ID)).thenReturn(null);

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () ->
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
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () ->
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
        ValidationException exception = assertThrows(ValidationException.class, () ->
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
        Instant existingDeactivationDate = now.plus(10, ChronoUnit.DAYS); // Changed to future date
        previousVersion.setValidBetween(new ValidBetween(now.minus(365, ChronoUnit.DAYS), existingDeactivationDate));

        when(vehicleTypeRepository.findFirstByNetexIdOrderByVersionDesc(NETEX_ID)).thenReturn(previousVersion);
        when(authorizationService.canDeleteEntity(previousVersion)).thenReturn(true);

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () ->
                vehicleTypeDeactivator.deactivateVehicleType(NETEX_ID, VERSION, futureDate)
        );

        assertTrue(exception.getMessage().contains("already deactivated") ||
                exception.getMessage().contains(NETEX_ID));
        verify(authorizationService).canDeleteEntity(previousVersion);
        verify(versionCreator, never()).createCopy(any(), any());
        verify(vehicleTypeVersionedSaverService, never()).saveNewVersion(any(), any(), any());
    }

    @Test
    void deactivateVehicleType_DeactivateToday_Success() throws ValidationException {
        // Given
        // Use a timestamp slightly in the future to avoid validation failure
        Instant nearFuture = now.plus(1, ChronoUnit.SECONDS);
        nextVersion.setValidBetween(new ValidBetween(now, nearFuture));

        when(vehicleTypeRepository.findFirstByNetexIdOrderByVersionDesc(NETEX_ID)).thenReturn(previousVersion);
        when(authorizationService.canDeleteEntity(previousVersion)).thenReturn(true);
        when(versionCreator.createCopy(previousVersion, VehicleType.class)).thenReturn(nextVersion);
        when(vehicleTypeVersionedSaverService.saveNewVersion(eq(previousVersion), eq(nextVersion), any(Instant.class)))
                .thenReturn(nextVersion);

        // When
        VehicleType result = vehicleTypeDeactivator.deactivateVehicleType(NETEX_ID, VERSION, nearFuture);

        // Then
        assertNotNull(result);
        assertEquals(nearFuture, nextVersion.getValidBetween().getToDate());
        verify(vehicleTypeVersionedSaverService).saveNewVersion(eq(previousVersion), eq(nextVersion), any(Instant.class));
    }

    @Test
    void deactivateVehicleType_DeactivationTimeWithPrecision_Success() throws ValidationException {
        // Given
        // Test that time element (not just date) is properly handled
        Instant preciseTime = now.plus(15, ChronoUnit.DAYS)
                .plus(3, ChronoUnit.HOURS)
                .plus(30, ChronoUnit.MINUTES);

        VehicleType nextVer = createVehicleType(NETEX_ID, VERSION + 1, now, preciseTime);

        when(vehicleTypeRepository.findFirstByNetexIdOrderByVersionDesc(NETEX_ID)).thenReturn(previousVersion);
        when(authorizationService.canDeleteEntity(previousVersion)).thenReturn(true);
        when(versionCreator.createCopy(previousVersion, VehicleType.class)).thenReturn(nextVer);
        when(vehicleTypeVersionedSaverService.saveNewVersion(any(), any(), any()))
                .thenReturn(nextVer);

        // When
        VehicleType result = vehicleTypeDeactivator.deactivateVehicleType(NETEX_ID, VERSION, preciseTime);

        // Then
        assertNotNull(result);
        assertEquals(preciseTime, result.getValidBetween().getToDate());
    }


    @Test
    void deactivateVehicleType_ValidBetweenNotNull_Success() throws ValidationException {
        // Given
        String netexId = "VT:007";
        Long expectedVersion = 10L;

        VehicleType prevVersion = createVehicleType(netexId, 10L, now.minus(365, ChronoUnit.DAYS), null);
        VehicleType nextVer = createVehicleType(netexId, 11L, now, futureDate);

        when(vehicleTypeRepository.findFirstByNetexIdOrderByVersionDesc(netexId)).thenReturn(prevVersion);
        when(authorizationService.canDeleteEntity(prevVersion)).thenReturn(true);
        when(versionCreator.createCopy(prevVersion, VehicleType.class)).thenReturn(nextVer);
        lenient().when(vehicleTypeVersionedSaverService.saveNewVersion(eq(prevVersion), eq(nextVer), any()))
                .thenReturn(nextVer);

        // When
        VehicleType result = vehicleTypeDeactivator.deactivateVehicleType(netexId, expectedVersion, futureDate);

        // Then
        assertNotNull(result);
        verify(vehicleTypeVersionedSaverService).saveNewVersion(eq(prevVersion), eq(nextVer), any(Instant.class));
    }

    private VehicleType createVehicleType(String netexId, Long version, Instant fromDate, Instant toDate) {
        VehicleType vehicleType = new VehicleType();
        vehicleType.setNetexId(netexId);
        vehicleType.setVersion(version);
        vehicleType.setValidBetween(new ValidBetween(fromDate, toDate));
        return vehicleType;
    }

    private void assertInstantCloseTo(Instant actual, Instant expected, long toleranceSeconds) {
        long diffSeconds = Math.abs(Duration.between(expected, actual).getSeconds());
        assertTrue(diffSeconds <= toleranceSeconds,
                String.format("Expected <%s> to be within %d seconds of <%s> but difference was %d seconds",
                        actual, toleranceSeconds, expected, diffSeconds));
    }
}
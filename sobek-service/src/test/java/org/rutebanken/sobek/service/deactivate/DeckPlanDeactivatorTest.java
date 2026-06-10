/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

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
import org.rutebanken.sobek.model.vehicle.DeckPlan;
import org.rutebanken.sobek.repository.DeckPlanRepository;
import org.rutebanken.sobek.repository.VehicleTypeRepository;
import org.rutebanken.sobek.versioning.VersionCreator;
import org.rutebanken.sobek.versioning.save.DeckPlanVersionedSaverService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeckPlanDeactivatorTest {

    @Mock
    private DeckPlanVersionedSaverService deckPlanVersionedSaverService;

    @Mock
    private DeckPlanRepository deckPlanRepository;

    @Mock
    private VehicleTypeRepository vehicleTypeRepository;

    @Mock
    private UsernameFetcher usernameFetcher;

    @Mock
    private VersionCreator versionCreator;

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private DeckPlanDeactivator deckPlanDeactivator;

    private static final String NETEX_ID = "TEST:DeckPlan:1";
    private static final Long VERSION = 1L;
    private static final String USERNAME = "testuser";

    private DeckPlan previousDeckPlan;
    private DeckPlan nextVersionDeckPlan;
    private Instant today;
    private Instant futureDate;

    @BeforeEach
    void setUp() {
        today = Instant.now().truncatedTo(ChronoUnit.DAYS);
        futureDate = today.plus(30, ChronoUnit.DAYS);

        previousDeckPlan = new DeckPlan();
        previousDeckPlan.setNetexId(NETEX_ID);
        previousDeckPlan.setVersion(VERSION);
        previousDeckPlan.setValidBetween(new ValidBetween(today.minus(30, ChronoUnit.DAYS), null));

        nextVersionDeckPlan = new DeckPlan();
        nextVersionDeckPlan.setNetexId(NETEX_ID);
        nextVersionDeckPlan.setVersion(VERSION + 1);

        when(usernameFetcher.getUserNameForAuthenticatedUser()).thenReturn(USERNAME);
    }

    @Test
    void deactivateDeckPlan_Success() {
        // Arrange
        when(deckPlanRepository.findFirstByNetexIdOrderByVersionDesc(NETEX_ID)).thenReturn(previousDeckPlan);
        when(authorizationService.canDeleteEntity(previousDeckPlan)).thenReturn(true);
        when(vehicleTypeRepository.existsValidWithDeckPlan(NETEX_ID, VERSION)).thenReturn(false);
        when(versionCreator.createCopy(previousDeckPlan, DeckPlan.class)).thenReturn(nextVersionDeckPlan);
        when(deckPlanVersionedSaverService.saveNewVersion(eq(previousDeckPlan), eq(nextVersionDeckPlan), any(Instant.class)))
                .thenReturn(nextVersionDeckPlan);

        // Act
        DeckPlan result = deckPlanDeactivator.deactivateDeckPlan(NETEX_ID, VERSION, futureDate);

        // Assert
        assertNotNull(result);
        assertEquals(today, previousDeckPlan.getValidBetween().getToDate());
        assertEquals(today, nextVersionDeckPlan.getValidBetween().getFromDate());
        assertEquals(futureDate, nextVersionDeckPlan.getValidBetween().getToDate());
        verify(deckPlanVersionedSaverService).saveNewVersion(eq(previousDeckPlan), eq(nextVersionDeckPlan), any(Instant.class));
    }

    @Test
    void deactivateDeckPlan_DeactivationDateInPast_UsesToday() {
        // Arrange
        Instant pastDate = today.minus(10, ChronoUnit.DAYS);
        when(deckPlanRepository.findFirstByNetexIdOrderByVersionDesc(NETEX_ID)).thenReturn(previousDeckPlan);
        when(authorizationService.canDeleteEntity(previousDeckPlan)).thenReturn(true);
        when(vehicleTypeRepository.existsValidWithDeckPlan(NETEX_ID, VERSION)).thenReturn(false);
        when(versionCreator.createCopy(previousDeckPlan, DeckPlan.class)).thenReturn(nextVersionDeckPlan);
        when(deckPlanVersionedSaverService.saveNewVersion(eq(previousDeckPlan), eq(nextVersionDeckPlan), any(Instant.class)))
                .thenReturn(nextVersionDeckPlan);

        // Act
        DeckPlan result = deckPlanDeactivator.deactivateDeckPlan(NETEX_ID, VERSION, pastDate);

        // Assert
        assertNotNull(result);
        assertEquals(today, nextVersionDeckPlan.getValidBetween().getToDate());
        verify(deckPlanVersionedSaverService).saveNewVersion(eq(previousDeckPlan), eq(nextVersionDeckPlan), any(Instant.class));
    }

    @Test
    void deactivateDeckPlan_DeckPlanNotFound_ThrowsException() {
        // Arrange
        when(deckPlanRepository.findFirstByNetexIdOrderByVersionDesc(NETEX_ID)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                deckPlanDeactivator.deactivateDeckPlan(NETEX_ID, VERSION, futureDate)
        );

        assertEquals("Cannot find deck plan to deactivate: " + NETEX_ID + ". No changes executed.", exception.getMessage());
        verify(deckPlanVersionedSaverService, never()).saveNewVersion(any(), any(), any());
    }

    @Test
    void deactivateDeckPlan_UnauthorizedUser_ThrowsException() {
        // Arrange
        when(deckPlanRepository.findFirstByNetexIdOrderByVersionDesc(NETEX_ID)).thenReturn(previousDeckPlan);
        when(authorizationService.canDeleteEntity(previousDeckPlan)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                deckPlanDeactivator.deactivateDeckPlan(NETEX_ID, VERSION, futureDate)
        );

        assertEquals("User is not authorized to deactivate deck plan " + NETEX_ID, exception.getMessage());
        verify(deckPlanVersionedSaverService, never()).saveNewVersion(any(), any(), any());
    }

    @Test
    void deactivateDeckPlan_VersionMismatch_ThrowsException() {
        // Arrange
        Long wrongVersion = 2L;
        when(deckPlanRepository.findFirstByNetexIdOrderByVersionDesc(NETEX_ID)).thenReturn(previousDeckPlan);
        when(authorizationService.canDeleteEntity(previousDeckPlan)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                deckPlanDeactivator.deactivateDeckPlan(NETEX_ID, wrongVersion, futureDate)
        );

        assertTrue(exception.getMessage().contains("has a different version than expected"));
        assertTrue(exception.getMessage().contains("Expected version: " + wrongVersion));
        assertTrue(exception.getMessage().contains("actual version: " + VERSION));
        verify(deckPlanVersionedSaverService, never()).saveNewVersion(any(), any(), any());
    }

    @Test
    void deactivateDeckPlan_AlreadyDeactivated_ThrowsException() {
        // Arrange
        previousDeckPlan.getValidBetween().setToDate(today.plus(10, ChronoUnit.DAYS));
        when(deckPlanRepository.findFirstByNetexIdOrderByVersionDesc(NETEX_ID)).thenReturn(previousDeckPlan);
        when(authorizationService.canDeleteEntity(previousDeckPlan)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                deckPlanDeactivator.deactivateDeckPlan(NETEX_ID, VERSION, futureDate)
        );

        assertTrue(exception.getMessage().contains("is already deactivated at"));
        verify(deckPlanVersionedSaverService, never()).saveNewVersion(any(), any(), any());
    }

    @Test
    void deactivateDeckPlan_StillInUse_ThrowsException() {
        // Arrange
        when(deckPlanRepository.findFirstByNetexIdOrderByVersionDesc(NETEX_ID)).thenReturn(previousDeckPlan);
        when(authorizationService.canDeleteEntity(previousDeckPlan)).thenReturn(true);
        when(vehicleTypeRepository.existsValidWithDeckPlan(NETEX_ID, VERSION)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                deckPlanDeactivator.deactivateDeckPlan(NETEX_ID, VERSION, futureDate)
        );

        assertEquals("Cannot deactivate deck plan " + NETEX_ID + " because it is still in use.", exception.getMessage());
        verify(deckPlanVersionedSaverService, never()).saveNewVersion(any(), any(), any());
    }

    @Test
    void deactivateDeckPlan_ValidBetweenIsNull_ThrowsNullPointerException() {
        // Arrange
        previousDeckPlan.setValidBetween(null);
        when(deckPlanRepository.findFirstByNetexIdOrderByVersionDesc(NETEX_ID)).thenReturn(previousDeckPlan);
        when(authorizationService.canDeleteEntity(previousDeckPlan)).thenReturn(true);
        when(vehicleTypeRepository.existsValidWithDeckPlan(NETEX_ID, VERSION)).thenReturn(false);
        when(versionCreator.createCopy(previousDeckPlan, DeckPlan.class)).thenReturn(nextVersionDeckPlan);

        // Act & Assert
        assertThrows(NullPointerException.class, () ->
                deckPlanDeactivator.deactivateDeckPlan(NETEX_ID, VERSION, futureDate)
        );

        verify(deckPlanVersionedSaverService, never()).saveNewVersion(any(), any(), any());
    }

    @Test
    void deactivateDeckPlan_DeactivateToday_Success() {
        // Arrange
        when(deckPlanRepository.findFirstByNetexIdOrderByVersionDesc(NETEX_ID)).thenReturn(previousDeckPlan);
        when(authorizationService.canDeleteEntity(previousDeckPlan)).thenReturn(true);
        when(vehicleTypeRepository.existsValidWithDeckPlan(NETEX_ID, VERSION)).thenReturn(false);
        when(versionCreator.createCopy(previousDeckPlan, DeckPlan.class)).thenReturn(nextVersionDeckPlan);
        when(deckPlanVersionedSaverService.saveNewVersion(eq(previousDeckPlan), eq(nextVersionDeckPlan), any(Instant.class)))
                .thenReturn(nextVersionDeckPlan);

        // Act
        DeckPlan result = deckPlanDeactivator.deactivateDeckPlan(NETEX_ID, VERSION, today);

        // Assert
        assertNotNull(result);
        assertEquals(today, nextVersionDeckPlan.getValidBetween().getToDate());
        verify(deckPlanVersionedSaverService).saveNewVersion(eq(previousDeckPlan), eq(nextVersionDeckPlan), any(Instant.class));
    }
}
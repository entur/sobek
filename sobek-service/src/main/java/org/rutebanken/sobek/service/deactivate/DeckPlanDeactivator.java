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

import org.rutebanken.sobek.auth.AuthorizationService;
import org.rutebanken.sobek.auth.UsernameFetcher;
import org.rutebanken.sobek.model.ValidBetween;
import org.rutebanken.sobek.model.vehicle.DeckPlan;
import org.rutebanken.sobek.repository.DeckPlanRepository;
import org.rutebanken.sobek.repository.VehicleTypeRepository;
import org.rutebanken.sobek.versioning.VersionCreator;
import org.rutebanken.sobek.versioning.save.DeckPlanVersionedSaverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class DeckPlanDeactivator {

    private static final Logger logger = LoggerFactory.getLogger(DeckPlanDeactivator.class);

    private final DeckPlanVersionedSaverService deckPlanVersionedSaverService;

    private final DeckPlanRepository deckPlanRepository;

    private final VehicleTypeRepository vehicleTypeRepository;

    private final UsernameFetcher usernameFetcher;

    private final VersionCreator versionCreator;

    private final AuthorizationService authorizationService;

    public DeckPlanDeactivator(DeckPlanVersionedSaverService deckPlanVersionedSaverService, DeckPlanRepository deckPlanRepository, VehicleTypeRepository vehicleTypeRepository, UsernameFetcher usernameFetcher, VersionCreator versionCreator, AuthorizationService authorizationService) {
        this.deckPlanVersionedSaverService = deckPlanVersionedSaverService;
        this.deckPlanRepository = deckPlanRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
        this.usernameFetcher = usernameFetcher;
        this.versionCreator = versionCreator;
        this.authorizationService = authorizationService;
    }

    public DeckPlan deactivateDeckPlan(String netexId, Long expectedVersion, Instant suggestedTimeOfDeactivation) {

        Instant today = Instant.now().truncatedTo(ChronoUnit.DAYS);
        Instant timeOfDeactivation;

        if (suggestedTimeOfDeactivation.isBefore(today)) {
            logger.warn("Deactivation date {} cannot be before today {}. Setting today as time of deactivation for {}", suggestedTimeOfDeactivation, today, netexId);
            timeOfDeactivation = today;
        } else {
            timeOfDeactivation = suggestedTimeOfDeactivation;
        }

        logger.info("User {} is deactivating deck plan {} at {}", usernameFetcher.getUserNameForAuthenticatedUser(), netexId, timeOfDeactivation);

        DeckPlan previousDeckPlanVersion = deckPlanRepository.findFirstByNetexIdOrderByVersionDesc(netexId);

        if (previousDeckPlanVersion != null) {

            // Deck Plan Saver service should always check that the user is authorized
            if(!authorizationService.canDeleteEntity(previousDeckPlanVersion)) {
                throw new IllegalArgumentException("User is not authorized to deactivate deck plan " + netexId);
            }

            if (previousDeckPlanVersion.getVersion() != expectedVersion) {
                throw new IllegalArgumentException("The deck plan " + netexId + " has a different version than expected. Expected version: " + expectedVersion + ", actual version: " + previousDeckPlanVersion.getVersion());
            }

            if (previousDeckPlanVersion.getValidBetween() != null && previousDeckPlanVersion.getValidBetween().getToDate() != null) {
                throw new IllegalArgumentException("The deck plan " + netexId + ", version " + previousDeckPlanVersion.getVersion() + " is already deactivated at " + previousDeckPlanVersion.getValidBetween().getToDate());
            }

            if(vehicleTypeRepository.existsValidWithDeckPlan(netexId, expectedVersion)) {
                throw new IllegalArgumentException("Cannot deactivate deck plan " + netexId + " because it is still in use.");
            }

            DeckPlan nextVersionDeckPlan = versionCreator.createCopy(previousDeckPlanVersion, DeckPlan.class);

            logger.debug("End previous version {} of deck plan {} at {} (today)", previousDeckPlanVersion.getVersion(), previousDeckPlanVersion.getNetexId(), today);
            previousDeckPlanVersion.getValidBetween().setToDate(today);

            nextVersionDeckPlan.setValidBetween(new ValidBetween(today, timeOfDeactivation));
            logger.debug("Set valid betwen to {} for new version of deck plan {}", nextVersionDeckPlan.getValidBetween(), nextVersionDeckPlan.getNetexId());


            return deckPlanVersionedSaverService.saveNewVersion(previousDeckPlanVersion, nextVersionDeckPlan, today);
        } else {
            throw new IllegalArgumentException("Cannot find deck plan to deactivate: " + netexId + ". No changes executed.");
        }
    }
}

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
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.repository.VehicleTypeRepository;
import org.rutebanken.sobek.versioning.VersionCreator;
import org.rutebanken.sobek.versioning.save.VehicleTypeVersionedSaverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class VehicleTypeDeactivator {

    private static final Logger logger = LoggerFactory.getLogger(VehicleTypeDeactivator.class);

    private final VehicleTypeVersionedSaverService vehicleTypeVersionedSaverService;

    private final VehicleTypeRepository vehicleTypeRepository;

    private final UsernameFetcher usernameFetcher;

    private final VersionCreator versionCreator;

    private final AuthorizationService authorizationService;

    public VehicleTypeDeactivator(VehicleTypeVersionedSaverService vehicleTypeVersionedSaverService, VehicleTypeRepository vehicleTypeRepository, UsernameFetcher usernameFetcher, VersionCreator versionCreator, AuthorizationService authorizationService) {
        this.vehicleTypeVersionedSaverService = vehicleTypeVersionedSaverService;
        this.vehicleTypeRepository = vehicleTypeRepository;
        this.usernameFetcher = usernameFetcher;
        this.versionCreator = versionCreator;
        this.authorizationService = authorizationService;
    }

    public VehicleType deactivateVehicleType(String netexId, Long expectedVersion, Instant suggestedTimeOfDeactivation) {

        Instant today = Instant.now().truncatedTo(ChronoUnit.DAYS);
        Instant timeOfDeactivation;

        if (suggestedTimeOfDeactivation.isBefore(today)) {
            logger.warn("Deactivation date {} cannot be before today {}. Setting today as time of deactivation for {}", suggestedTimeOfDeactivation, today, netexId);
            timeOfDeactivation = today;
        } else {
            timeOfDeactivation = suggestedTimeOfDeactivation;
        }

        logger.info("User {} is deactivating vehicle type {} at {}", usernameFetcher.getUserNameForAuthenticatedUser(), netexId, timeOfDeactivation);

        VehicleType previousVehicleTypeVersion = vehicleTypeRepository.findFirstByNetexIdOrderByVersionDesc(netexId);

        if (previousVehicleTypeVersion != null) {

            // Vehicle Type Saver service should always check that the user is authorized
            if(!authorizationService.canDeleteEntity(previousVehicleTypeVersion)) {
                throw new IllegalArgumentException("User is not authorized to deactivate vehicle type " + netexId);
            }

            if (previousVehicleTypeVersion.getVersion() != expectedVersion) {
                throw new IllegalArgumentException("The vehicle type " + netexId + " has a different version than expected. Expected version: " + expectedVersion + ", actual version: " + previousVehicleTypeVersion.getVersion());
            }

            if (previousVehicleTypeVersion.getValidBetween() != null && previousVehicleTypeVersion.getValidBetween().getToDate() != null) {
                throw new IllegalArgumentException("The vehicle type " + netexId + ", version " + previousVehicleTypeVersion.getVersion() + " is already deactivated at " + previousVehicleTypeVersion.getValidBetween().getToDate());
            }

            if(previousVehicleTypeVersion.getVehicles() != null && !previousVehicleTypeVersion.getVehicles().isEmpty()) {
                throw new IllegalArgumentException("Cannot deactivate vehicle type " + netexId + " because it is still in use.");
            }

            VehicleType nextVersionVehicleType = versionCreator.createCopy(previousVehicleTypeVersion, VehicleType.class);

            logger.debug("End previous version {} of vehicle type {} at {} (today)", previousVehicleTypeVersion.getVersion(), previousVehicleTypeVersion.getNetexId(), today);
            previousVehicleTypeVersion.getValidBetween().setToDate(today);

            nextVersionVehicleType.setValidBetween(new ValidBetween(today, timeOfDeactivation));
            logger.debug("Set valid betwen to {} for new version of vehicle type {}", nextVersionVehicleType.getValidBetween(), nextVersionVehicleType.getNetexId());


            return vehicleTypeVersionedSaverService.saveNewVersion(previousVehicleTypeVersion, nextVersionVehicleType, today);
        } else {
            throw new IllegalArgumentException("Cannot find vehicle type to deactivate: " + netexId + ". No changes executed.");
        }
    }
}

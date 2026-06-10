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
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.repository.VehicleRepository;
import org.rutebanken.sobek.versioning.VersionCreator;
import org.rutebanken.sobek.versioning.save.VehicleVersionedSaverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class VehicleDeactivator {

    private static final Logger logger = LoggerFactory.getLogger(VehicleDeactivator.class);

    private final VehicleVersionedSaverService vehicleVersionedSaverService;

    private final VehicleRepository vehicleRepository;

    private final UsernameFetcher usernameFetcher;

    private final VersionCreator versionCreator;

    private final AuthorizationService authorizationService;

    public VehicleDeactivator(VehicleVersionedSaverService vehicleVersionedSaverService, VehicleRepository vehicleRepository, UsernameFetcher usernameFetcher, VersionCreator versionCreator, AuthorizationService authorizationService) {
        this.vehicleVersionedSaverService = vehicleVersionedSaverService;
        this.vehicleRepository = vehicleRepository;
        this.usernameFetcher = usernameFetcher;
        this.versionCreator = versionCreator;
        this.authorizationService = authorizationService;
    }

    public Vehicle deactivateVehicle(String netexId, Long expectedVersion, Instant suggestedTimeOfDeactivation) {

        Instant today = Instant.now().truncatedTo(ChronoUnit.DAYS);
        Instant timeOfDeactivation;

        if (suggestedTimeOfDeactivation.isBefore(today)) {
            logger.warn("Deactivation date {} cannot be before today {}. Setting today as time of deactivation for {}", suggestedTimeOfDeactivation, today, netexId);
            timeOfDeactivation = today;
        } else {
            timeOfDeactivation = suggestedTimeOfDeactivation;
        }

        logger.info("User {} is deactivating vehicle {} at {}", usernameFetcher.getUserNameForAuthenticatedUser(), netexId, timeOfDeactivation);

        Vehicle previousVehicleVersion = vehicleRepository.findFirstByNetexIdOrderByVersionDesc(netexId);

        if (previousVehicleVersion != null) {

            // Vehicle Saver service should always check that the user is authorized
            if(!authorizationService.canDeleteEntity(previousVehicleVersion)) {
                throw new IllegalArgumentException("User is not authorized to deactivate vehicle " + netexId);
            }

            if (previousVehicleVersion.getVersion() != expectedVersion) {
                throw new IllegalArgumentException("The vehicle " + netexId + " has a different version than expected. Expected version: " + expectedVersion + ", actual version: " + previousVehicleVersion.getVersion());
            }

            if (previousVehicleVersion.getValidBetween() != null && previousVehicleVersion.getValidBetween().getToDate() != null) {
                throw new IllegalArgumentException("The vehicle " + netexId + ", version " + previousVehicleVersion.getVersion() + " is already deactivated at " + previousVehicleVersion.getValidBetween().getToDate());
            }

            Vehicle nextVersionVehicle = versionCreator.createCopy(previousVehicleVersion, Vehicle.class);

            logger.debug("End previous version {} of vehicle {} at {} (today)", previousVehicleVersion.getVersion(), previousVehicleVersion.getNetexId(), today);
            previousVehicleVersion.getValidBetween().setToDate(today);

            nextVersionVehicle.setValidBetween(new ValidBetween(today, timeOfDeactivation));
            logger.debug("Set valid betwen to {} for new version of vehicle {}", nextVersionVehicle.getValidBetween(), nextVersionVehicle.getNetexId());


            return vehicleVersionedSaverService.saveNewVersion(previousVehicleVersion, nextVersionVehicle, today);
        } else {
            throw new IllegalArgumentException("Cannot find vehicle to deactivate: " + netexId + ". No changes executed.");
        }
    }
}

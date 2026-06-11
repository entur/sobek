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

import jakarta.xml.bind.ValidationException;
import org.rutebanken.sobek.auth.AuthorizationService;
import org.rutebanken.sobek.auth.UsernameFetcher;
import org.rutebanken.sobek.model.EntityInVersionStructure;
import org.rutebanken.sobek.model.ValidBetween;
import org.rutebanken.sobek.versioning.VersionCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;

public class GenericDeactivator<T extends EntityInVersionStructure> {

    @FunctionalInterface
    public interface TriFunction<One, Two, Three, R> {
        R apply(One one, Two two, Three three);
    }

    private static final Logger logger = LoggerFactory.getLogger(GenericDeactivator.class);

    private final UsernameFetcher usernameFetcher;

    private final VersionCreator versionCreator;

    private final AuthorizationService authorizationService;

    private final Class<T> classT;

    public GenericDeactivator(UsernameFetcher usernameFetcher, VersionCreator versionCreator, AuthorizationService authorizationService, Class<T> classT) {
        this.usernameFetcher = usernameFetcher;
        this.versionCreator = versionCreator;
        this.authorizationService = authorizationService;
        this.classT = classT;
    }

    public T deactivateObject(String netexId, Long expectedVersion, Instant deactivateAt, String typeName, Function<String, T> findFirstByNetexIdOrderByVersionDesc, TriFunction<T, T, Instant, T> saveNewVersion, Function<T, String> typeSpecificValidation) throws ValidationException {
        Instant now = Instant.now();
        if (deactivateAt.isBefore(now)) {
            // Allow 1 minute grace period for deactivating objects, interpret that as "now"
            if(deactivateAt.isBefore(now.minus(1, ChronoUnit.MINUTES))) {
                throw new ValidationException("Deactivation date cannot be set backwards in time");
            } else {
                logger.warn("Deactivation date {} cannot be before now {}. Setting now as deactivation date for {}", deactivateAt, now, netexId);
                deactivateAt = now;
            }
        }
        logger.info("User {} is deactivating {} {} at {}", usernameFetcher.getUserNameForAuthenticatedUser(), typeName, netexId, deactivateAt);

        T previousVersion = findFirstByNetexIdOrderByVersionDesc.apply(netexId);

        if (previousVersion != null) {
            // Object Saver service should always check that the user is authorized
            if(!authorizationService.canDeleteEntity(previousVersion)) {
                throw new AccessDeniedException("User is not authorized to deactivate " + typeName + " " + netexId);
            }

            if (previousVersion.getVersion() != expectedVersion) {
                throw new ValidationException("The " + typeName + " " + netexId + " has a different version than expected. Expected version: " + expectedVersion + ", actual version: " + previousVersion.getVersion());
            }

            if (previousVersion.getValidBetween() != null && previousVersion.getValidBetween().getToDate() != null) {
                throw new ValidationException("The " + typeName + " " + netexId + ", version " + previousVersion.getVersion() + " is already deactivated at " + previousVersion.getValidBetween().getToDate());
            }

            String extraValidationError = typeSpecificValidation.apply(previousVersion);
            if(extraValidationError != null) {
                throw new ValidationException(extraValidationError);
            }

            T nextVersion = versionCreator.createCopy(previousVersion, classT);

            logger.debug("End previous version {} of {} {} at {} ", typeName, previousVersion.getVersion(), previousVersion.getNetexId(), deactivateAt);
            previousVersion.getValidBetween().setToDate(now);

            nextVersion.setValidBetween(new ValidBetween(now, deactivateAt));
            logger.debug("Set valid between to {} for new version of {} {}", nextVersion.getValidBetween(), typeName, nextVersion.getNetexId());

            return saveNewVersion.apply(previousVersion, nextVersion, now);
        } else {
            throw new ValidationException("Cannot find " + typeName + " to deactivate: " + netexId + ". No changes executed.");
        }
    }
}

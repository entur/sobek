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

package org.rutebanken.sobek.versioning.save;

import org.rutebanken.sobek.auth.AuthorizationService;
import org.rutebanken.sobek.auth.UsernameFetcher;
import org.rutebanken.sobek.diff.SobekObjectDiffer;
import org.rutebanken.sobek.model.DataManagedObjectStructure;
import org.rutebanken.sobek.model.EntityInVersionStructure;
import org.rutebanken.sobek.model.authorization.OwnedEntity;
import org.rutebanken.sobek.organisation.OrganisationRegistry;
import org.rutebanken.sobek.repository.EntityInVersionRepository;
import org.rutebanken.sobek.repository.listener.NetexIdAssigner;
import org.rutebanken.sobek.versioning.ValidityUpdater;
import org.rutebanken.sobek.versioning.VersionIncrementor;
import org.rutebanken.sobek.versioning.validate.VersionValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;

@Service
public class DefaultMergingVersionedSaverService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultMergingVersionedSaverService.class);

    public static final int MILLIS_BETWEEN_VERSIONS = 1;

    private final UsernameFetcher usernameFetcher;
    private final ValidityUpdater validityUpdater;
    private final SobekObjectDiffer sobekObjectDiffer;
    private final VersionIncrementor versionIncrementor;
    private final NetexIdAssigner netexIdAssigner;
//    @Autowired
//    private PrometheusMetricsService prometheusMetricsService;

    private final AuthorizationService authorizationService;
    private final VersionValidator versionValidator;
    private final OrganisationRegistry organisationRegistry;

    public DefaultMergingVersionedSaverService(UsernameFetcher usernameFetcher, ValidityUpdater validityUpdater, SobekObjectDiffer sobekObjectDiffer, VersionIncrementor versionIncrementor, NetexIdAssigner netexIdAssigner, AuthorizationService authorizationService, VersionValidator versionValidator, OrganisationRegistry organisationRegistry) {
        this.usernameFetcher = usernameFetcher;
        this.validityUpdater = validityUpdater;
        this.sobekObjectDiffer = sobekObjectDiffer;
        this.versionIncrementor = versionIncrementor;
        this.netexIdAssigner = netexIdAssigner;
        this.authorizationService = authorizationService;
        this.versionValidator = versionValidator;
        this.organisationRegistry = organisationRegistry;
    }

    public <T extends EntityInVersionStructure> T saveNewVersion(T newVersion, EntityInVersionRepository<T> entityInVersionRepository) {
        return saveNewVersion(null, newVersion, Instant.now(), entityInVersionRepository);
    }

    public <T extends EntityInVersionStructure> T saveNewVersion(T existingVersion, T newVersion, EntityInVersionRepository<T> entityInVersionRepository) {
        return saveNewVersion(existingVersion, newVersion, Instant.now(), entityInVersionRepository);
    }

    public <T extends EntityInVersionStructure> T saveNewVersion(T existingVersion, T newVersion, Instant defaultValidFrom, EntityInVersionRepository<T> entityInVersionRepository) {

        if (newVersion instanceof OwnedEntity ownedEntity) {
            organisationRegistry.validateOrganisationRef(ownedEntity.getDataOwnerRef());
        }

        versionValidator.validate(existingVersion, newVersion);

        Instant newVersionValidFrom = validityUpdater.updateValidBetween(existingVersion, newVersion, defaultValidFrom);

        if(existingVersion == null) {
            if (newVersion.getNetexId() == null) {
                netexIdAssigner.assignNetexId(newVersion);
            } else {
                existingVersion = entityInVersionRepository.findFirstByNetexIdOrderByVersionDesc(newVersion.getNetexId());
                if (existingVersion != null) {
                    logger.debug("Found existing entity from netexId {}", existingVersion.getNetexId());
                }
            }
        }

        authorizeNewVersion(existingVersion, newVersion);

        if(existingVersion == null) {
            newVersion.setCreated(defaultValidFrom);
            // If the new incoming version has the version attribute set, reset it.
            // For sobek, this is the first time this entity with this ID is saved
            newVersion.setVersion(-1L);
        } else {
            newVersion.setVersion(existingVersion.getVersion());
            newVersion.setChanged(defaultValidFrom);
            validityUpdater.terminateVersion(existingVersion, newVersionValidFrom.minusMillis(MILLIS_BETWEEN_VERSIONS));
            entityInVersionRepository.save(existingVersion);
        }

        versionIncrementor.initiateOrIncrement(newVersion);

        String usernameForAuthenticatedUser = usernameFetcher.getUserNameForAuthenticatedUser();
        if(newVersion instanceof DataManagedObjectStructure) {
            ((DataManagedObjectStructure) newVersion).setChangedBy(usernameForAuthenticatedUser);
        }

        if(existingVersion != null) {
            newVersion.mergeWithExistingVersion(existingVersion);
        }

        logger.info("Object {}, version {} changed by user {}", newVersion.getNetexId(), newVersion.getVersion(), usernameForAuthenticatedUser);

        newVersion = entityInVersionRepository.save(newVersion);
        if(existingVersion != null) {
            sobekObjectDiffer.logDifference(existingVersion, newVersion);
        }
        //prometheusMetricsService.registerEntitySaved(newVersion.getClass(),1L);
        return newVersion;
    }

    private <T extends EntityInVersionStructure> void authorizeNewVersion(T existingVersion, T newVersion) {
        authorizationService.verifyCanEditEntities( Arrays.asList(existingVersion, newVersion));
    }



}

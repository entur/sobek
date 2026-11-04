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

import jakarta.validation.ValidationException;
import org.rutebanken.sobek.auth.AuthorizationService;
import org.rutebanken.sobek.auth.UsernameFetcher;
import org.rutebanken.sobek.diff.SobekObjectDiffer;
import org.rutebanken.sobek.model.DataManagedObjectStructure;
import org.rutebanken.sobek.model.EntityInVersionStructure;
import org.rutebanken.sobek.model.authorization.OwnedEntity;
import org.rutebanken.sobek.netex.id.ValidPrefixList;
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

import static org.rutebanken.sobek.model.CustomKeyValueTypes.ORIGINAL_ID_KEY;

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
    private final ValidPrefixList validPrefixList;

    public DefaultMergingVersionedSaverService(UsernameFetcher usernameFetcher, ValidityUpdater validityUpdater, SobekObjectDiffer sobekObjectDiffer, VersionIncrementor versionIncrementor, NetexIdAssigner netexIdAssigner, AuthorizationService authorizationService, VersionValidator versionValidator, OrganisationRegistry organisationRegistry, ValidPrefixList validPrefixList) {
        this.validPrefixList = validPrefixList;
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
                } else {
                    handleClientIdOfNewObject(newVersion);
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

    /**
     * A new object arrives with an id that does not exist. What happens depends on whose codespace it is in:
     * <ul>
     *   <li>Sobek's own (NMR, CVT): rejected, as only Sobek assigns ids there. (An existing id is an update.)</li>
     *   <li>A registered codespace: the client's id is respected and used as the NeTEx id.</li>
     *   <li>Any other (e.g. Autosys): Sobek assigns an id of its own and keeps the external id as imported-id.</li>
     * </ul>
     */
    private <T extends EntityInVersionStructure> void handleClientIdOfNewObject(T newVersion) {
        String clientId = newVersion.getNetexId();
        switch (validPrefixList.getCodespaceOf(clientId, newVersion.getClass())) {
            case SOBEK -> throw new ValidationException("No " + newVersion.getClass().getSimpleName() + " with id " + clientId
                    + " exists, and ids in this codespace are assigned by Sobek. To create a new object, leave out the id"
                    + " or use an id in your own codespace.");
            case REGISTERED -> logger.debug("Using the id {} that the client has assigned", clientId);
            case EXTERNAL -> {
                if (newVersion instanceof DataManagedObjectStructure dataManaged) {
                    logger.debug("Id {} is from an external codespace. Assigning an id and keeping it as {}", clientId, ORIGINAL_ID_KEY);
                    dataManaged.removeKeyValue(ORIGINAL_ID_KEY);
                    dataManaged.addKeyValue(ORIGINAL_ID_KEY, clientId);
                    newVersion.setNetexId(null);
                    netexIdAssigner.assignNetexId(newVersion);
                } else {
                    logger.warn("Id {} is from an external codespace, but the object cannot hold key values. Keeping the id", clientId);
                }
            }
        }
    }

    private <T extends EntityInVersionStructure> void authorizeNewVersion(T existingVersion, T newVersion) {
        authorizationService.verifyCanEditEntities( Arrays.asList(existingVersion, newVersion));
    }



}

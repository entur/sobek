
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

package org.rutebanken.sobek.organisation;

import org.rutebanken.netex.model.OrganisationTypeEnumeration;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Organisation registry that loads organisations from a JSON HTTP endpoint returning NannaOrganisation objects.
 * This implementation is activated when the nanna.organisations.uri property is configured.
 */
@Component
@ConditionalOnProperty(name = "nanna.organisations.uri")
public class NannaOrganisationRegistry extends AbstractCachedOrganisationRegistry {

    private final WebClient authorizedOrgRegisterClient;
    private final String nannaHttpUri;

    private final Logger logger = LoggerFactory.getLogger(NannaOrganisationRegistry.class);

    public NannaOrganisationRegistry(
        @Value("${nanna.organisations.uri}") String nannaHttpUri,
        @Value("${sobek.organisations.cache-duration-seconds:3600}") String cacheDurationSeconds,
        WebClient authorizedOrgRegisterClient
    ) {
        super(cacheDurationSeconds);
        this.nannaHttpUri = nannaHttpUri;
        this.authorizedOrgRegisterClient = authorizedOrgRegisterClient;
    }

    @Override
    protected List<Organisation> loadOrganisationsFromSource() throws Exception {
        List<Organisation> loadedOrganisations = new ArrayList<>();

        logger.info("Loading organisations from Nanna JSON endpoint: {}", nannaHttpUri);

        List<NannaOrganisation> nannaOrgs = authorizedOrgRegisterClient
            .get()
            .uri(nannaHttpUri)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<NannaOrganisation>>() {})
            .block(Duration.ofSeconds(30));

        if (nannaOrgs == null || nannaOrgs.isEmpty()) {
            logger.warn("No organisations received from Nanna JSON endpoint");
            return List.of();
        }

        logger.debug("Parsed {} organisations from Nanna JSON", nannaOrgs.size());

        // Convert NannaOrganisation to Organisation
        for (NannaOrganisation nannaOrg : nannaOrgs) {
            try {
                OrganisationTypeEnumeration type = mapOrganisationType(nannaOrg.organisationType());

                Organisation org = new Organisation(
                    nannaOrg.privateCode(),
                    new EmbeddableMultilingualString(nannaOrg.name()),
                    type
                );

                loadedOrganisations.add(org);
                logger.debug("Mapped organisation: id={}, name={}, type={}",
                    nannaOrg.privateCode(), nannaOrg.name(), type);
            } catch (Exception e) {
                logger.warn("Failed to map organisation with id {}: {}", nannaOrg.id(), e.getMessage());
            }
        }

        logger.info("Loaded {} organisations from Nanna JSON endpoint", loadedOrganisations.size());
        return loadedOrganisations;
    }

    /**
     * Maps the string organisation type from JSON to the enumeration.
     *
     * @param typeString The organisation type as string
     * @return The corresponding OrganisationTypeEnumeration
     */
    private OrganisationTypeEnumeration mapOrganisationType(String typeString) {
        if (typeString == null || typeString.isBlank()) {
            return OrganisationTypeEnumeration.OTHER;
        }

        return switch (typeString.toUpperCase()) {
            case "AUTHORITY" -> OrganisationTypeEnumeration.AUTHORITY;
            case "OPERATOR" -> OrganisationTypeEnumeration.OPERATOR;
            default -> OrganisationTypeEnumeration.OTHER;
        };
    }
}
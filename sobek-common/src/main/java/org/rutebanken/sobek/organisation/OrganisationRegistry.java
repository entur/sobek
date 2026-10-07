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

/* Copied from UTTU and refined for Sobek needs */

package org.rutebanken.sobek.organisation;

import java.util.List;
import java.util.Optional;

import org.rutebanken.sobek.error.CodedIllegalArgumentException;

/**
 * Represents an organisation registry used to populate authorities and operators references
 */
public interface OrganisationRegistry {
    /**
     * Get a list of all organisations in the registry
     */
    List<Organisation> getOrganisations();

    /**
     * Get an organisation with the given ID, which may not exist
     */
    Optional<Organisation> getOrganisation(String id);

    /**
     * Check if the organisation represented by the reference id is a valid organisation of any type (authority, operator, general)
     * @param organisationRef The organisation id
     * @throws CodedIllegalArgumentException if the organisation is not a valid organisation
     */
    void validateOrganisationRef(String organisationRef);

}

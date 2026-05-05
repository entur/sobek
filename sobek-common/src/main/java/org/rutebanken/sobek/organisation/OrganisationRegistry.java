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

import org.rutebanken.netex.model.GeneralOrganisation;
import org.rutebanken.netex.model.Organisation_VersionStructure;
import org.rutebanken.sobek.error.CodedIllegalArgumentException;
import org.rutebanken.netex.model.Authority;
import org.rutebanken.netex.model.Operator;

/**
 * Represents an organisation registry used to populate authorities and operators references
 */
public interface OrganisationRegistry {
    /**
     * Get a list of all general organisations in the registry
     */
    List<GeneralOrganisation> getGeneralOrganisations();

    /**
     * Get a general organisation with the given ID, which may not exist
     */
    Optional<GeneralOrganisation> getGeneralOrganisation(String id);

    /**
     * Get a list of all organisations in the registry
     */
    List<Organisation_VersionStructure> getOrganisations();

    /**
     * Get an organisation with the given ID, which may not exist
     */
    Optional<Organisation_VersionStructure> getOrganisation(String id);

    /**
     * Get a list of all authorities in the registry
     */
    List<Authority> getAuthorities();

    /**
     * Get an authority with the given ID, which may not exist
     */
    Optional<Authority> getAuthority(String id);

    /**
     * Get a list of all operators in the registry
     */
    List<Operator> getOperators();

    /**
     * Get an operator with the given ID, which may not exist
     */
    Optional<Operator> getOperator(String id);

    /**
     * Check if the organisation represented by the operator reference id is a valid operator
     * @param operatorRef The organisation id
     * @throws CodedIllegalArgumentException if the organisation is not a valid operator
     */
    void validateOperatorRef(String operatorRef);

    /**
     * Check if the organisation represented by the operator reference id is a valid authority
     * @param authorityRef The organisation id
     * @throws CodedIllegalArgumentException if the organisation is not a valid authority
     */
    void validateAuthorityRef(String authorityRef);

    /**
     * Check if the organisation represented by the reference id is a valid general organisation
     * @param generalOrganisationRef The organisation id
     * @throws CodedIllegalArgumentException if the organisation is not a valid general organisation
     */
    void validateGeneralOrganisationRef(String generalOrganisationRef);

}

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.xml.transform.Source;

import org.rutebanken.netex.model.*;
import org.rutebanken.sobek.error.CodedError;
import org.rutebanken.sobek.error.ErrorCodeEnumeration;
import org.rutebanken.sobek.netex.marshal.NetexUnmarshaller;
import org.rutebanken.sobek.netex.marshal.NetexUnmarshallerUnmarshalFromSourceException;
import org.rutebanken.sobek.netex.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class NetexPublicationDeliveryOrganisationRegistry
        implements OrganisationRegistry {

    private final Logger logger = LoggerFactory.getLogger(
            NetexPublicationDeliveryOrganisationRegistry.class
    );
    private final NetexUnmarshaller netexUnmarshaller = new NetexUnmarshaller(
            PublicationDeliveryStructure.class
    );

    private final List<Organisation_VersionStructure> organisations = Collections.synchronizedList(
            new ArrayList<>()
    );

    public void init() {
        try {
            PublicationDeliveryStructure publicationDeliveryStructure =
                    netexUnmarshaller.unmarshalFromSource(getPublicationDeliverySource());
            publicationDeliveryStructure
                    .getDataObjects()
                    .getCompositeFrameOrCommonFrame()
                    .forEach(frame -> {
                        var frameValue = frame.getValue();
                        if (frameValue instanceof ResourceFrame resourceFrame) {
                            resourceFrame
                                    .getOrganisations()
                                    .getOrganisation_Dummy()
                                    .forEach(org -> {
                                        if (Organisation_VersionStructure.class.isAssignableFrom(org.getDeclaredType())) {
                                            organisations.add((Organisation_VersionStructure)org.getValue());
                                        } else {
                                            throw new UnsupportedOrganisationTypeException(org.getDeclaredType());
                                        }
                                    });
                        }
                    });
            logger.info("Organisations loaded from organisations xml");
        } catch (NetexUnmarshallerUnmarshalFromSourceException e) {
            logger.warn(
                    "Unable to unmarshal organisations xml, organisation registry will be an empty list",
                    e
            );
        }
    }

    protected abstract Source getPublicationDeliverySource();

    @Override
    public List<Authority> getAuthorities() {
        return organisations.stream().filter(org -> org instanceof Authority).map(org -> (Authority) org).toList();
    }

    @Override
    public Optional<Authority> getAuthority(String id) {
        return organisations
                .stream()
                .filter(authority -> authority.getId().equals(id) && authority instanceof Authority)
                .map(org -> (Authority)org)
                .findFirst();
    }

    @Override
    public List<GeneralOrganisation> getGeneralOrganisations() {
        return organisations.stream().filter(org -> org instanceof GeneralOrganisation).map(org -> (GeneralOrganisation) org).toList();
    }

    @Override
    public Optional<GeneralOrganisation> getGeneralOrganisation(String id) {
        return organisations
                .stream()
                .filter(org -> org.getId().equals(id) && org instanceof GeneralOrganisation)
                .map(org -> (GeneralOrganisation)org)
                .findFirst();
    }

    @Override
    public List<Organisation_VersionStructure> getOrganisations() {
        return Collections.unmodifiableList(organisations);
    }

    @Override
    public Optional<Organisation_VersionStructure> getOrganisation(String id) {
        return organisations
                .stream()
                .filter(org -> org.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Operator> getOperators() {
        return organisations.stream().filter(org -> org instanceof Operator).map(org -> (Operator) org).toList();
    }

    @Override
    public Optional<Operator> getOperator(String id) {
        return organisations.stream().filter(operator -> operator.getId().equals(id)).map(org -> (Operator)org).findFirst();
    }

    /**
     * By default, all organisations in the registry are valid operators
     */
    @Override
    public void validateOperatorRef(String operatorRef) {
        Preconditions.checkArgument(
                organisations.stream().anyMatch(org -> org.getId().equals(operatorRef) && org instanceof Operator),
                CodedError.fromErrorCode(
                        ErrorCodeEnumeration.ORGANISATION_NOT_IN_ORGANISATION_REGISTRY
                ),
                "Organisation with ref %s not found in organisation registry",
                operatorRef
        );
    }

    /**
     * By default, all organisations in the registry are valid authorities
     */
    @Override
    public void validateAuthorityRef(String authorityRef) {
        Preconditions.checkArgument(
                organisations.stream().anyMatch(org -> org.getId().equals(authorityRef) && org instanceof Authority),
                CodedError.fromErrorCode(
                        ErrorCodeEnumeration.ORGANISATION_NOT_IN_ORGANISATION_REGISTRY
                ),
                "Organisation with ref %s not found in organisation registry",
                authorityRef
        );
    }

    /**
     * By default, all organisations in the registry are valid authorities
     */
    @Override
    public void validateGeneralOrganisationRef(String generalOrganisationRef) {
        Preconditions.checkArgument(
                organisations.stream().anyMatch(org -> org.getId().equals(generalOrganisationRef) && org instanceof GeneralOrganisation),
                CodedError.fromErrorCode(
                        ErrorCodeEnumeration.ORGANISATION_NOT_IN_ORGANISATION_REGISTRY
                ),
                "Organisation with ref %s not found in organisation registry",
                generalOrganisationRef
        );
    }


}

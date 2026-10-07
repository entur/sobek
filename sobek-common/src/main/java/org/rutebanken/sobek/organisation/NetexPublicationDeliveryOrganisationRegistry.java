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

import org.rutebanken.netex.model.*;
import org.rutebanken.sobek.netex.mapping.mapstruct.MultilingualStringMapper;
import org.rutebanken.sobek.netex.marshal.NetexUnmarshaller;
import org.rutebanken.sobek.netex.marshal.NetexUnmarshallerUnmarshalFromSourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.xml.transform.Source;
import java.util.ArrayList;
import java.util.List;

/**
 * Organisation registry that loads organisations from a NeTEx PublicationDelivery XML source.
 * This implementation extends the generic cached registry and provides NeTEx-specific loading logic.
 */
public abstract class NetexPublicationDeliveryOrganisationRegistry
    extends AbstractCachedOrganisationRegistry {

    private static final Logger logger = LoggerFactory.getLogger(NetexPublicationDeliveryOrganisationRegistry.class);

    @Autowired
    private MultilingualStringMapper multilingualStringMapper;


    private final NetexUnmarshaller netexUnmarshaller = new NetexUnmarshaller(
        PublicationDeliveryStructure.class
    );

    public NetexPublicationDeliveryOrganisationRegistry(
        @Value("${sobek.organisations.cache-duration-seconds:3600}") String cacheDurationSeconds
    ) {
        super(cacheDurationSeconds);
    }

    @Override
    protected List<Organisation> loadOrganisationsFromSource() throws Exception {
        List<Organisation_VersionStructure> netexOrganisations = new ArrayList<>();

        Source orgSource = getPublicationDeliverySource();
        if (orgSource == null) {
            logger.info("No data received when loading organisations from NeTEx source");
            return List.of();
        }

        try {
            PublicationDeliveryStructure publicationDeliveryStructure =
                netexUnmarshaller.unmarshalFromSource(orgSource);

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
                                    netexOrganisations.add((Organisation_VersionStructure) org.getValue());
                                } else {
                                    throw new UnsupportedOrganisationTypeException(org.getDeclaredType());
                                }
                            });
                    }
                });

            logger.info("Organisations loaded from NeTEx XML (total: {})", netexOrganisations.size());
        } catch (NetexUnmarshallerUnmarshalFromSourceException e) {
            logger.warn("Unable to unmarshal organisations xml", e);
            throw e;
        }

        // Convert NeTEx organisations to Organisation records
        return convertToOrganisations(netexOrganisations);
    }

    /**
     * Template method to be implemented by subclasses to provide the NeTEx XML source.
     *
     * @return Source containing the NeTEx PublicationDelivery XML
     */
    protected abstract Source getPublicationDeliverySource();

    /**
     * Converts NeTEx Organisation_VersionStructure objects to Organisation records.
     *
     * @param netexOrganisations List of NeTEx organisations
     * @return List of Organisation records
     */
    private List<Organisation> convertToOrganisations(List<Organisation_VersionStructure> netexOrganisations) {
        return netexOrganisations.stream()
            .map(netexOrg -> new Organisation(
                netexOrg.getId(),
                multilingualStringMapper.mapToSobek(netexOrg.getName()),
                getOrganisationType(netexOrg)
            ))
            .toList();
    }

    /**
     * Determines the organisation type from a NeTEx organisation object.
     *
     * @param org NeTEx organisation
     * @return OrganisationTypeEnumeration
     */
    private OrganisationTypeEnumeration getOrganisationType(Organisation_VersionStructure org) {
        if (org instanceof Authority) {
            return OrganisationTypeEnumeration.AUTHORITY;
        } else if (org instanceof Operator) {
            return OrganisationTypeEnumeration.OPERATOR;
        } else {
            return OrganisationTypeEnumeration.OTHER;
        }
    }
}
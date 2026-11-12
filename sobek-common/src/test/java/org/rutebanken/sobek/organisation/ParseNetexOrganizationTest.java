package org.rutebanken.sobek.organisation;

import jakarta.validation.ValidationException;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;
import org.entur.netex2.model.*;
import org.rutebanken.sobek.error.CodedException;
import org.rutebanken.sobek.error.ErrorCodeEnumeration;
import org.rutebanken.sobek.error.OrganisationRefViolationException;
import org.rutebanken.sobek.netex.marshal.PublicationDeliveryUnmarshaller;
import org.rutebanken.sobek.netex.util.PublicationDeliveryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class ParseNetexOrganizationTest {

    @Autowired
    PublicationDeliveryUnmarshaller publicationDeliveryUnmarshaller;
    @Autowired
    private PublicationDeliveryHelper publicationDeliveryHelper;

    @Autowired
    private NetexPublicationDeliveryOrganisationRegistry netexPublicationDeliveryOrganisationRegistry;

    @Test
    public void readFileRawTest() throws IOException, JAXBException, SAXException {
        PublicationDeliveryStructure publicationDelivery;
        try (InputStream in = getClass().getResourceAsStream("/fixtures/organisations-netex-dev.xml")) {
            publicationDelivery = publicationDeliveryUnmarshaller.unmarshal(in);
        }
        assertThat(publicationDelivery).isNotNull();

        assertThat(publicationDelivery.getDataObjects()).isNotNull();

        PublicationDeliveryStructure.DataObjects dataObjects =  publicationDelivery.getDataObjects();
        assertThat(dataObjects.getCompositeFrameOrCommonFrame()).isNotNull();

        ResourceFrame resourceFrame = publicationDeliveryHelper.findResourceFrame(publicationDelivery);
        assertThat(resourceFrame).isNotNull();
        assertThat(resourceFrame.getOrganisations()).isNotNull();

        OrganisationsInFrame_RelStructure organisations = resourceFrame.getOrganisations();

        assertThat(organisations.getOrganisation()).isNotNull()
                .isNotEmpty();
    }

    @Test
    public void readFileWithFileRegistryTest() {

        netexPublicationDeliveryOrganisationRegistry.validateOrganisationRef("NOG:GeneralOrganisation:l9B7EYodP6d");

        netexPublicationDeliveryOrganisationRegistry.validateOrganisationRef("NOG:Authority:c5HUG26214p");

        netexPublicationDeliveryOrganisationRegistry.validateOrganisationRef("NOG:Operator:eanaqt2T022");
    }

    @Test
    public void organisationThatIsNotInTheRegistryIsABadRequestWithItsCode() {
        assertThatThrownBy(() -> netexPublicationDeliveryOrganisationRegistry.validateOrganisationRef("NOG:Operator:does-not-exist"))
                // A ValidationException is a bad request to API users: an unknown organisation is the fault of the data
                .isInstanceOf(ValidationException.class)
                .isInstanceOf(OrganisationRefViolationException.class)
                .hasMessageContaining("NOG:Operator:does-not-exist")
                // And it keeps the code that says what is wrong
                .satisfies(e -> assertThat(((CodedException) e).getCode())
                        .isEqualTo(ErrorCodeEnumeration.ORGANISATION_NOT_IN_ORGANISATION_REGISTRY.toString()));
    }

    @Test
    public void blankOrganisationRefIsABadRequestWithItsCode() {
        for (String blank : new String[]{null, "", "   "}) {
            assertThatThrownBy(() -> netexPublicationDeliveryOrganisationRegistry.validateOrganisationRef(blank))
                    .as("organisation ref '%s'", blank)
                    .isInstanceOf(ValidationException.class)
                    .satisfies(e -> assertThat(((CodedException) e).getCode())
                            .isEqualTo(ErrorCodeEnumeration.ORGANISATION_REF_NULL.toString()));
        }
    }
}

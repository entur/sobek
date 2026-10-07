package org.rutebanken.sobek.organisation;

import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;
import org.rutebanken.netex.model.*;
import org.rutebanken.sobek.netex.marshal.PublicationDeliveryUnmarshaller;
import org.rutebanken.sobek.netex.util.PublicationDeliveryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(organisations.getOrganisation_Dummy()).isNotNull()
                .isNotEmpty();
    }

    @Test
    public void readFileWithFileRegistryTest() {

        netexPublicationDeliveryOrganisationRegistry.validateOrganisationRef("NOG:GeneralOrganisation:l9B7EYodP6d");

        netexPublicationDeliveryOrganisationRegistry.validateOrganisationRef("NOG:Authority:c5HUG26214p");

        netexPublicationDeliveryOrganisationRegistry.validateOrganisationRef("NOG:Operator:eanaqt2T022");
    }
}

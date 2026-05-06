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


        List<Operator> operators = organisations.getOrganisation_Dummy()
                .stream()
                .filter(org -> org.getValue() instanceof Operator)
                .map(org -> (Operator) org.getValue())
                .toList();

        validateOrganisations(operators, "985615616");

        List<Authority> authorities = organisations.getOrganisation_Dummy()
                .stream()
                .filter(org -> org.getValue() instanceof Authority)
                .map(org -> (Authority) org.getValue())
                .toList();
        validateOrganisations(authorities, "991609407");
    }

    private static void validateOrganisations(List<? extends Organisation_VersionStructure> organisations, String expectedCompanyNumber) {
        assertThat(organisations).isNotEmpty();
        assertThat(organisations.stream().filter(op -> op.getCompanyNumber() != null && op.getCompanyNumber().equals(expectedCompanyNumber)).count()).isEqualTo(1); // Find expected organisation
    }

    @Test
    public void readFileWithFileRegistryTest() {
        validateOrganisations(netexPublicationDeliveryOrganisationRegistry.getOperators(), "985615616");
        validateOrganisations(netexPublicationDeliveryOrganisationRegistry.getAuthorities(), "991609407");
        validateOrganisations(netexPublicationDeliveryOrganisationRegistry.getGeneralOrganisations(), "920285376");

        netexPublicationDeliveryOrganisationRegistry.validateGeneralOrganisationRef("NOG:GeneralOrganisation:l9B7EYodP6d");

        netexPublicationDeliveryOrganisationRegistry.validateAuthorityRef("NOG:Authority:c5HUG26214p");

        netexPublicationDeliveryOrganisationRegistry.validateOperatorRef("NOG:Operator:eanaqt2T022");
    }
}

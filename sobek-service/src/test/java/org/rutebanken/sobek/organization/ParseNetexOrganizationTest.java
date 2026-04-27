package org.rutebanken.sobek.organization;

import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.Test;
import org.rutebanken.netex.model.*;
import org.rutebanken.sobek.netex.mapping.PublicationDeliveryHelper;
import org.rutebanken.sobek.netex.util.PublicationDeliveryUnmarshaller;
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

    @Test
    public void readFileTest() throws IOException, JAXBException, SAXException {
        PublicationDeliveryStructure publicationDelivery;
        try (InputStream in = getClass().getResourceAsStream("/fixtures/organizations-netex-dev.xml")) {
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
        assertThat(operators).isNotEmpty();
        assertThat(operators.stream().filter(op -> op.getCompanyNumber() != null && op.getCompanyNumber().equals("985615616")).count()).isEqualTo(1); // Find UNIBUSS

        List<Authority> authorities = organisations.getOrganisation_Dummy()
                .stream()
                .filter(org -> org.getValue() instanceof Authority)
                .map(org -> (Authority) org.getValue())
                .toList();
        assertThat(authorities).isNotEmpty();
        assertThat(authorities.stream().filter(aut -> aut.getCompanyNumber() != null && aut.getCompanyNumber().equals("991609407")).count()).isEqualTo(1); // Find Ruter
    }
}

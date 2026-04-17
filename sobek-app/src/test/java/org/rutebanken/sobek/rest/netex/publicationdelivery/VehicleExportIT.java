package org.rutebanken.sobek.rest.netex.publicationdelivery;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rutebanken.netex.model.CompositeFrame;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.rutebanken.netex.model.ResourceFrame;
import org.rutebanken.netex.model.Vehicle;
import org.rutebanken.sobek.SobekTestApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = SobekTestApplication.class)
class VehicleExportIT {

    @LocalServerPort int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void exportVehicle_byId_returnsPublicationDeliveryWithOneMatchingVehicle() throws Exception {
        String seedXml;
        try (InputStream in = getClass().getResourceAsStream("/fixtures/vehicle-export-seed.xml")) {
            seedXml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        String importResponseXml =
                given()
                        .contentType(ContentType.XML)
                        .queryParam("importType", "ID_MATCH")
                        .body(seedXml)
                        .when()
                        .post("/services/vehicles/netex")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .asString();

        Vehicle importedVehicle = extractSingleVehicle(unmarshal(importResponseXml));
        assertThat(importedVehicle.getId()).isNotBlank();

        String exportXml =
                given()
                        .accept(ContentType.XML)
                        .when()
                        .get("/services/vehicles/netex/vehicles/" + importedVehicle.getId())
                        .then()
                        .statusCode(200)
                        .contentType(ContentType.XML.withCharset(StandardCharsets.UTF_8))
                        .extract()
                        .body()
                        .asString();

        Vehicle exportedVehicle = extractSingleVehicle(unmarshal(exportXml));
        assertThat(exportedVehicle.getId()).isEqualTo(importedVehicle.getId());
    }

    @Test
    void exportVehicle_unknownId_returnsEmptyResourceFrame() throws Exception {
        String exportXml =
                given()
                        .accept(ContentType.XML)
                        .when()
                        .get("/services/vehicles/netex/vehicles/NMR:Vehicle:does-not-exist")
                        .then()
                        .statusCode(200)
                        .contentType(ContentType.XML.withCharset(StandardCharsets.UTF_8))
                        .extract()
                        .body()
                        .asString();

        ResourceFrame frame = extractResourceFrame(unmarshal(exportXml));
        assertThat(frame.getVehicles()).isNull();
    }

    private static Vehicle extractSingleVehicle(PublicationDeliveryStructure pd) {
        ResourceFrame frame = extractResourceFrame(pd);
        assertThat(frame.getVehicles()).isNotNull();
        assertThat(frame.getVehicles().getVehicle()).hasSize(1);
        return frame.getVehicles().getVehicle().get(0);
    }

    private static ResourceFrame extractResourceFrame(PublicationDeliveryStructure pd) {
        return pd.getDataObjects().getCompositeFrameOrCommonFrame().stream()
                .map(JAXBElement::getValue)
                .flatMap(VehicleExportIT::flattenToResourceFrames)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No ResourceFrame in response"));
    }

    private static Stream<ResourceFrame> flattenToResourceFrames(Object frame) {
        if (frame instanceof ResourceFrame rf) {
            return Stream.of(rf);
        }
        if (frame instanceof CompositeFrame cf && cf.getFrames() != null) {
            return cf.getFrames().getCommonFrame().stream()
                    .map(JAXBElement::getValue)
                    .filter(ResourceFrame.class::isInstance)
                    .map(ResourceFrame.class::cast);
        }
        return Stream.empty();
    }

    private static final JAXBContext jaxbContext;

    static {
        try {
            jaxbContext = JAXBContext.newInstance(PublicationDeliveryStructure.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private PublicationDeliveryStructure unmarshal(String xml) throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        JAXBElement<PublicationDeliveryStructure> element =
                (JAXBElement<PublicationDeliveryStructure>)
                        unmarshaller.unmarshal(new StringReader(xml));
        return element.getValue();
    }
}

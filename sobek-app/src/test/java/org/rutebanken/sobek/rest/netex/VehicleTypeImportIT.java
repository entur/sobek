package org.rutebanken.sobek.rest.netex;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.rutebanken.netex.model.ResourceFrame;
import org.rutebanken.netex.model.TextType;
import org.rutebanken.netex.model.VehicleType;
import org.rutebanken.sobek.SobekTestApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = SobekTestApplication.class)
class VehicleTypeImportIT {

  @LocalServerPort int port;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
  }

  @Test
  void importVehicleType_returnsImportedVehicleTypeWithAssignedId() throws Exception {
    String xml;
    try (InputStream in = getClass().getResourceAsStream("/fixtures/vehicle-type-import.xml")) {
      xml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }

    String responseXml =
        given()
            .contentType("application/xml")
            .queryParam("importType", "ID_MATCH")
            .body(xml)
            .when()
            .post("/services/vehicles/netex")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .asString();

    PublicationDeliveryStructure response = unmarshal(responseXml);

    ResourceFrame responseFrame =
        response.getDataObjects().getCompositeFrameOrCommonFrame().stream()
            .map(jaxb -> jaxb.getValue())
            .filter(ResourceFrame.class::isInstance)
            .map(ResourceFrame.class::cast)
            .findFirst()
            .orElseThrow(() -> new AssertionError("No ResourceFrame in response"));

    assertThat(responseFrame.getVehicleTypes()).isNotNull();
    assertThat(responseFrame.getVehicleTypes().getTransportType_Dummy()).hasSize(1);

    VehicleType imported =
        (VehicleType)
            responseFrame.getVehicleTypes().getTransportType_Dummy().get(0).getValue();

    assertThat(imported.getId()).startsWith("NMR:VehicleType:");
    assertThat(imported.getVersion()).isEqualTo("1");
    TextType nameText =
        imported.getName().getContent().stream()
            .filter(JAXBElement.class::isInstance)
            .map(JAXBElement.class::cast)
            .map(JAXBElement::getValue)
            .filter(TextType.class::isInstance)
            .map(TextType.class::cast)
            .findFirst()
            .orElseThrow(
                () ->
                    new AssertionError(
                        "No TextType name element in imported vehicle type"));
    assertThat(nameText.getValue()).isEqualTo("Exqui City 24");
  }

    @Test
    void importVehicleTypeBasic_returnsImportedVehicleTypeWithAssignedId() throws Exception {
        String xml;
        try (InputStream in = getClass().getResourceAsStream("/fixtures/vehicle-type-import-basic.xml")) {
            xml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        String responseXml =
                given()
                        .contentType("application/xml")
                        .queryParam("importType", "ID_MATCH")
                        .body(xml)
                        .when()
                        .post("/services/vehicles/netex")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .asString();

        PublicationDeliveryStructure response = unmarshal(responseXml);

        ResourceFrame responseFrame =
                response.getDataObjects().getCompositeFrameOrCommonFrame().stream()
                        .map(jaxb -> jaxb.getValue())
                        .filter(ResourceFrame.class::isInstance)
                        .map(ResourceFrame.class::cast)
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("No ResourceFrame in response"));

        assertThat(responseFrame.getVehicleTypes()).isNotNull();
        assertThat(responseFrame.getVehicleTypes().getTransportType_Dummy()).hasSize(1);

        VehicleType imported =
                (VehicleType)
                        responseFrame.getVehicleTypes().getTransportType_Dummy().get(0).getValue();

        assertThat(imported.getId()).startsWith("AKT:VehicleType:");
        assertThat(imported.getVersion()).isEqualTo("1");
        assertThat(imported.getName().getContent()).hasSize(3);
        assertThat(imported.getName().getContent().get(1)).isInstanceOf(JAXBElement.class);
        JAXBElement<? extends  TextType> theName = (JAXBElement)imported.getName().getContent().get(1);
        assertThat(theName.getValue().getValue()).isEqualTo("Exqui City 24");
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
  private PublicationDeliveryStructure unmarshal(String xml) throws Exception {
    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    JAXBElement<PublicationDeliveryStructure> element =
        (JAXBElement<PublicationDeliveryStructure>)
            unmarshaller.unmarshal(new StringReader(xml));
    return element.getValue();
  }
}

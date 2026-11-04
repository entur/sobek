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
import org.entur.netex2.model.PublicationDeliveryStructure;
import org.entur.netex2.model.ResourceFrame;
import org.entur.netex2.model.VehicleType;
import org.rutebanken.sobek.model.vehicle.VehicleTypeKindEnumeration;
import org.rutebanken.sobek.repository.VehicleTypeRepository;
import org.rutebanken.sobek.versioning.save.VehicleTypeVersionedSaverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(SingleOrganisationAuthorizationConfiguration.class)
class VehicleTypeImportIT {

  @LocalServerPort int port;

  @Autowired private VehicleTypeVersionedSaverService vehicleTypeSaver;

  @Autowired private VehicleTypeRepository vehicleTypeRepository;

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
            .body(xml)
            .when()
            .post("/services/vehicles/netex?vehicleTypeKind=PHYSICAL")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .asString();

    PublicationDeliveryStructure response = unmarshal(responseXml);

    ResourceFrame responseFrame =
        response.getDataObjects().getCompositeFrameOrCommonFrame().stream()
            .map(JAXBElement::getValue)
            .filter(ResourceFrame.class::isInstance)
            .map(ResourceFrame.class::cast)
            .findFirst()
            .orElseThrow(() -> new AssertionError("No ResourceFrame in response"));

    assertThat(responseFrame.getVehicleTypes()).isNotNull();
    assertThat(responseFrame.getVehicleTypes().getTransportType()).hasSize(1);

    VehicleType imported =
        (VehicleType)
            responseFrame.getVehicleTypes().getTransportType().getFirst().getValue();

    assertThat(imported.getId()).startsWith("NMR:VehicleType:");
    assertThat(imported.getVersion()).isEqualTo("1");
      String nameText =
          imported.getName().getContent().stream()
              .filter(String.class::isInstance)
              .map(String.class::cast)
              .findFirst()
              .orElseThrow(
                  () ->
                      new AssertionError(
                          "No String name element in imported vehicle type"));
      assertThat(nameText).isEqualTo("Exaqui City 24");
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
                        .body(xml)
                        .when()
                        .post("/services/vehicles/netex?vehicleTypeKind=PHYSICAL")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .asString();

        PublicationDeliveryStructure response = unmarshal(responseXml);

        ResourceFrame responseFrame =
                response.getDataObjects().getCompositeFrameOrCommonFrame().stream()
                        .map(JAXBElement::getValue)
                        .filter(ResourceFrame.class::isInstance)
                        .map(ResourceFrame.class::cast)
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("No ResourceFrame in response"));

        assertThat(responseFrame.getVehicleTypes()).isNotNull();
        assertThat(responseFrame.getVehicleTypes().getTransportType()).hasSize(2);

        VehicleType imported =
                (VehicleType)
                        responseFrame.getVehicleTypes().getTransportType().getFirst().getValue();

        assertThat(imported.getId()).startsWith("AKT:VehicleType:");
        assertThat(imported.getName().getContent()).hasSize(1);
        assertThat(imported.getName().getContent().getFirst()).isInstanceOf(String.class);
        String theName = (String)imported.getName().getContent().getFirst();
        assertThat(theName).isEqualTo("Exqui City 24");
    }

    @Test
    void importUpdateOfContractualVehicleType_keepsItsContractualId() throws Exception {
        // A contractual vehicle type cannot be created by import (ids in CVT are Sobek's), but it can be updated
        String id = vehicleTypeSaver.saveNewVersion(contractualVehicleType()).getNetexId();
        assertThat(id).startsWith("CVT:VehicleType:");

        String responseXml =
                given()
                        .contentType("application/xml")
                        .body(fixture("/fixtures/vehicle-type-import-with-id.xml").replace("@ID@", id))
                        .when()
                        .post("/services/vehicles/netex?vehicleTypeKind=PHYSICAL")
                        .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .asString();

        ResourceFrame responseFrame =
                unmarshal(responseXml).getDataObjects().getCompositeFrameOrCommonFrame().stream()
                        .map(JAXBElement::getValue)
                        .filter(ResourceFrame.class::isInstance)
                        .map(ResourceFrame.class::cast)
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("No ResourceFrame in response"));
        VehicleType imported =
                (VehicleType) responseFrame.getVehicleTypes().getTransportType().getFirst().getValue();

        assertThat(imported.getId()).isEqualTo(id);
        assertThat(vehicleTypeRepository.findFirstByNetexIdOrderByVersionDesc(id).getVehicleTypeKind())
                .isEqualTo(VehicleTypeKindEnumeration.CONTRACTUAL);
    }

    @Test
    void importVehicleReferringToContractualVehicleType_isBadRequest() throws Exception {
        String contractualId = vehicleTypeSaver.saveNewVersion(contractualVehicleType()).getNetexId();

        String body =
                given()
                        .contentType("application/xml")
                        .body(fixture("/fixtures/vehicle-import-with-type-ref.xml")
                                .replace("@ID@", "AKT:Vehicle:contractual-ref-" + System.nanoTime())
                                .replace("@TYPE_ID@", contractualId))
                        .when()
                        .post("/services/vehicles/netex?vehicleTypeKind=PHYSICAL")
                        .then()
                        .statusCode(400)
                        .extract()
                        .body()
                        .asString();

        assertThat(body).contains("contractual");
    }

    private static org.rutebanken.sobek.model.vehicle.VehicleType contractualVehicleType() {
        var type = new org.rutebanken.sobek.model.vehicle.VehicleType();
        type.setTransportMode(org.rutebanken.sobek.model.vehicle.AllPublicTransportModesEnumeration.BUS);
        type.setDataOwnerRef(SingleOrganisationAuthorizationConfiguration.DATA_OWNER);
        type.setVehicleTypeKind(VehicleTypeKindEnumeration.CONTRACTUAL);
        return type;
    }

    private String fixture(String path) throws Exception {
        try (InputStream in = getClass().getResourceAsStream(path)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
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

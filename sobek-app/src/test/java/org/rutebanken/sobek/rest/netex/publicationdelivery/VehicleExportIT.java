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
import org.entur.netex2.model.CompositeFrame;
import org.entur.netex2.model.PublicationDeliveryStructure;
import org.entur.netex2.model.ResourceFrame;
import org.entur.netex2.model.Vehicle;
import org.rutebanken.sobek.repository.DeckPlanRepository;
import org.rutebanken.sobek.repository.VehicleRepository;
import org.rutebanken.sobek.rest.netex.SingleOrganisationAuthorizationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(SingleOrganisationAuthorizationConfiguration.class)
class VehicleExportIT {

    @LocalServerPort int port;

    private static final String DATA_OWNER = SingleOrganisationAuthorizationConfiguration.DATA_OWNER;

    /** A valid organisation the test user is not authorized for. */
    private static final String OTHER_DATA_OWNER = "ATB";

    @Autowired private VehicleRepository vehicleRepository;

    @Autowired private DeckPlanRepository deckPlanRepository;

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
                        .queryParam("dataOwner", DATA_OWNER)
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
                        .queryParam("dataOwner", DATA_OWNER)
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

    @Test
    void exportVehicle_withoutDataOwner_returnsBadRequest() {
        given()
                .accept(ContentType.XML)
                .when()
                .get("/services/vehicles/netex/vehicles/NMR:Vehicle:does-not-exist")
                .then()
                .statusCode(400);
    }

    @Test
    void exportDeckPlan_withoutDataOwner_returnsBadRequest() {
        given()
                .accept(ContentType.XML)
                .when()
                .get("/services/vehicles/netex/deckplans/NMR:DeckPlan:does-not-exist")
                .then()
                .statusCode(400);
    }

    @Test
    void exportVehicle_dataOwnerUserIsNotAuthorizedFor_returnsSameForbiddenWhetherOrNotTheVehicleExists() {
        saveVehicleOwnedBy(OTHER_DATA_OWNER, "ATB:Vehicle:forbidden-existing");

        String existing = exportExpectingForbidden("/services/vehicles/netex/vehicles/ATB:Vehicle:forbidden-existing");
        String missing = exportExpectingForbidden("/services/vehicles/netex/vehicles/ATB:Vehicle:does-not-exist");

        assertThat(existing).isEqualTo(missing);
    }

    @Test
    void exportDeckPlan_dataOwnerUserIsNotAuthorizedFor_returnsSameForbiddenWhetherOrNotTheDeckPlanExists() {
        saveDeckPlanOwnedBy(OTHER_DATA_OWNER, "ATB:DeckPlan:forbidden-existing");

        String existing = exportExpectingForbidden("/services/vehicles/netex/deckplans/ATB:DeckPlan:forbidden-existing");
        String missing = exportExpectingForbidden("/services/vehicles/netex/deckplans/ATB:DeckPlan:does-not-exist");

        assertThat(existing).isEqualTo(missing);
    }

    @Test
    void exportVehicle_ownedByOtherDataOwner_isTreatedAsNotFound() throws Exception {
        saveVehicleOwnedBy(OTHER_DATA_OWNER, "ATB:Vehicle:other-owner");

        ResourceFrame frame = extractResourceFrame(unmarshal(
                exportExpectingOk("/services/vehicles/netex/vehicles/ATB:Vehicle:other-owner")));

        assertThat(frame.getVehicles()).isNull();
    }

    @Test
    void exportDeckPlan_ownedByOtherDataOwner_isTreatedAsNotFound() throws Exception {
        saveDeckPlanOwnedBy(OTHER_DATA_OWNER, "ATB:DeckPlan:other-owner");

        ResourceFrame frame = extractResourceFrame(unmarshal(
                exportExpectingOk("/services/vehicles/netex/deckplans/ATB:DeckPlan:other-owner")));

        assertThat(frame.getDeckPlans()).isNull();
    }

    /** Saved directly, as the test user is not allowed to import data for other data owners. */
    private void saveVehicleOwnedBy(String dataOwnerRef, String netexId) {
        org.rutebanken.sobek.model.vehicle.Vehicle vehicle = new org.rutebanken.sobek.model.vehicle.Vehicle();
        vehicle.setNetexId(netexId);
        vehicle.setDataOwnerRef(dataOwnerRef);
        vehicleRepository.save(vehicle);
    }

    /** Saved directly, as the test user is not allowed to import data for other data owners. */
    private void saveDeckPlanOwnedBy(String dataOwnerRef, String netexId) {
        org.rutebanken.sobek.model.vehicle.DeckPlan deckPlan = new org.rutebanken.sobek.model.vehicle.DeckPlan();
        deckPlan.setNetexId(netexId);
        deckPlan.setDataOwnerRef(dataOwnerRef);
        deckPlanRepository.save(deckPlan);
    }

    /** Requests the export as the test user's own data owner. */
    private static String exportExpectingOk(String path) {
        return given()
                .accept(ContentType.XML)
                .queryParam("dataOwner", DATA_OWNER)
                .when()
                .get(path)
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();
    }

    /** Requests the export as a data owner the test user is not authorized for. */
    private static String exportExpectingForbidden(String path) {
        return given()
                .accept(ContentType.XML)
                .queryParam("dataOwner", OTHER_DATA_OWNER)
                .when()
                .get(path)
                .then()
                .statusCode(403)
                .extract()
                .body()
                .asString();
    }

    private static Vehicle extractSingleVehicle(PublicationDeliveryStructure pd) {
        ResourceFrame frame = extractResourceFrame(pd);
        assertThat(frame.getVehicles()).isNotNull();
        assertThat(frame.getVehicles().getVehicle()).hasSize(1);
        return frame.getVehicles().getVehicle().getFirst();
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

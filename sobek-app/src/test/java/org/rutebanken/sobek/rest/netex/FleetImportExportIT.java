package org.rutebanken.sobek.rest.netex;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import org.entur.netex2.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rutebanken.sobek.model.KeyValue;
import org.rutebanken.sobek.model.identification.IdentifiedEntity;
import org.rutebanken.sobek.repository.FleetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Import and export of NeTEx Fleet through the REST API, with schema validation on in both directions. The fleet is in a
 * MobilityServiceFrame, and refers to vehicles and a vehicle type in the ResourceFrame of the same delivery.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(SingleOrganisationAuthorizationConfiguration.class)
class FleetImportExportIT {

    @LocalServerPort int port;

    @Autowired private FleetRepository fleetRepository;
    @Autowired private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("A fleet is imported with its members and transport types, answered in a MobilityServiceFrame, and exported again")
    void importThenExportKeepsMembersAndTransportTypes() throws Exception {
        String run = String.valueOf(System.nanoTime());
        String fleetId = "AKT:Fleet:run" + run + "f";
        String vehicle1 = "AKT:Vehicle:run" + run + "v1";
        String vehicle2 = "AKT:Vehicle:run" + run + "v2";
        String vehicleType = "AKT:VehicleType:run" + run + "t";

        String importResponse = importFleet(fixture(run));

        // The response has the fleet, in a mobility service frame, with references to the members
        List<Fleet> imported = fleets(importResponse);
        assertThat(imported).extracting(Fleet::getId).containsExactly(fleetId);
        assertThat(memberRefs(imported.getFirst())).containsExactly(vehicle1, vehicle2);

        // The database has the fleet as the client sent it, with the vehicles and the vehicle type it refers to
        transactionTemplate.executeWithoutResult(status -> {
            var stored = fleetRepository.findFirstByNetexIdOrderByVersionDesc(fleetId);
            assertThat(stored.getVersion()).isEqualTo(1L);
            assertThat(stored.getName().getValue()).isEqualTo("City buses");
            assertThat(stored.getShortName().getValue()).isEqualTo("CB");
            assertThat(stored.getDescription().getValue()).isEqualTo("The buses of the city lines");
            assertThat(stored.getPrivateCode().getValue()).isEqualTo("CB-1");
            assertThat(stored.getDataOwnerRef()).isEqualTo("AKT");
            assertThat(stored.getMembers()).extracting(IdentifiedEntity::getNetexId).containsExactlyInAnyOrder(vehicle1, vehicle2);
            assertThat(stored.getTransportTypes()).extracting(IdentifiedEntity::getNetexId).containsExactly(vehicleType);
            assertThat(stored.getKeyValues()).extracting(KeyValue::getKey).contains("depot");
        });

        // The export has the fleet in a mobility service frame next to the resource frame, and it is valid against the schema
        String exportXml = export("PHYSICAL");
        Fleet exported = fleets(exportXml).stream().filter(f -> fleetId.equals(f.getId())).findFirst()
                .orElseThrow(() -> new AssertionError("The fleet is not in the export"));
        assertThat(exported.getName().getContent()).isNotEmpty();
        assertThat(memberRefs(exported)).containsExactlyInAnyOrder(vehicle1, vehicle2);
        assertThat(exported.getTransportTypes().getTransportTypeRef())
                .extracting(JAXBElement::getValue).extracting(TransportTypeRefStructure::getRef).containsExactly(vehicleType);
        assertThat(exported.getKeyList().getKeyValue()).extracting(KeyValueStructure::getKey).contains("depot");
    }

    @Test
    @DisplayName("Fleets are exported with the physical vehicles they refer to, and not in an export of contractual vehicle types only")
    void contractualExportHasNoFleets() throws Exception {
        String run = String.valueOf(System.nanoTime());
        importFleet(fixture(run));

        assertThat(export("PHYSICAL")).contains("id=\"AKT:Fleet:run" + run + "f\"");
        assertThat(export("CONTRACTUAL")).doesNotContain("AKT:Fleet:run" + run + "f");
    }

    @Test
    @DisplayName("Importing a fleet again makes a new version, and the members are the ones of the new file")
    void importingAgainMakesANewVersionWithTheNewMembers() throws Exception {
        String run = String.valueOf(System.nanoTime());
        String fleetId = "AKT:Fleet:run" + run + "f";
        importFleet(fixture(run));

        // The second file has the same fleet with one of the vehicles only, and is a delivery of the frame of the fleet
        String second = fixture(run).replace("<VehicleRef ref=\"AKT:Vehicle:run" + run + "v2\"/>", "");
        importFleet(second);

        transactionTemplate.executeWithoutResult(status -> {
            var stored = fleetRepository.findFirstByNetexIdOrderByVersionDesc(fleetId);
            assertThat(stored.getVersion()).isEqualTo(2L);
            assertThat(stored.getMembers()).extracting(IdentifiedEntity::getNetexId).containsExactly("AKT:Vehicle:run" + run + "v1");
            // The first version still has both: it is history
            assertThat(fleetRepository.findFirstByNetexIdAndVersion(fleetId, 1L).getMembers()).hasSize(2);
        });
    }

    @Test
    @DisplayName("A fleet that has a member that does not exist is rejected")
    void fleetWithUnknownMemberIsBadRequest() throws Exception {
        String run = String.valueOf(System.nanoTime());
        String xml = fixture(run).replace("<VehicleRef ref=\"AKT:Vehicle:run" + run + "v2\"/>",
                "<VehicleRef ref=\"AKT:Vehicle:missing" + run + "\"/>");

        String body = given().contentType(ContentType.XML).body(xml)
                .when().post("/services/vehicles/netex?vehicleTypeKind=PHYSICAL")
                .then().statusCode(400)
                .extract().body().asString();

        assertThat(body).contains("AKT:Vehicle:missing" + run);
    }

    @Test
    @DisplayName("A fleet that has a transport type that does not exist is rejected")
    void fleetWithUnknownTransportTypeIsBadRequest() throws Exception {
        String run = String.valueOf(System.nanoTime());
        String xml = fixture(run).replace("<TransportTypeRef ref=\"AKT:VehicleType:run" + run + "t\"/>\n                    </transportTypes>",
                "<TransportTypeRef ref=\"AKT:VehicleType:missing" + run + "\"/>\n                    </transportTypes>");

        String body = given().contentType(ContentType.XML).body(xml)
                .when().post("/services/vehicles/netex?vehicleTypeKind=PHYSICAL")
                .then().statusCode(400)
                .extract().body().asString();

        assertThat(body).contains("AKT:VehicleType:missing" + run);
    }

    @Test
    @DisplayName("A vehicle inside a fleet is rejected: the members are given as references to vehicles that exist")
    void fleetWithInlineVehicleIsBadRequest() throws Exception {
        String run = String.valueOf(System.nanoTime());
        String xml = fixture(run).replace("<VehicleRef ref=\"AKT:Vehicle:run" + run + "v2\"/>",
                "<Vehicle id=\"AKT:Vehicle:run" + run + "inline\" version=\"1\"><Name lang=\"nb\">Inline</Name></Vehicle>");

        String body = given().contentType(ContentType.XML).body(xml)
                .when().post("/services/vehicles/netex?vehicleTypeKind=PHYSICAL")
                .then().statusCode(400)
                .extract().body().asString();

        assertThat(body).contains("VehicleRef");
    }

    private String importFleet(String xml) {
        return given().contentType(ContentType.XML).body(xml)
                .when().post("/services/vehicles/netex?vehicleTypeKind=PHYSICAL")
                .then().statusCode(200)
                .extract().body().asString();
    }

    private static String export(String kind) {
        return given().accept(ContentType.XML)
                .queryParam("dataOwner", SingleOrganisationAuthorizationConfiguration.DATA_OWNER)
                .queryParam("vehicleTypeKind", kind)
                .when().get("/services/vehicles/netex")
                .then().statusCode(200)
                .extract().body().asString();
    }

    private static List<String> memberRefs(Fleet fleet) {
        return fleet.getMembers().getVehicleRefOrVehicle().stream()
                .map(JAXBElement::getValue)
                .map(VehicleRefStructure.class::cast)
                .map(VehicleRefStructure::getRef)
                .toList();
    }

    /** The fleets in all the mobility service frames of the delivery, whether the frame is in the delivery or in a composite frame. */
    private static List<Fleet> fleets(String xml) throws Exception {
        Unmarshaller unmarshaller = JAXBContext.newInstance(PublicationDeliveryStructure.class).createUnmarshaller();
        @SuppressWarnings("unchecked")
        PublicationDeliveryStructure delivery = ((JAXBElement<PublicationDeliveryStructure>) unmarshaller.unmarshal(new StringReader(xml))).getValue();
        return delivery.getDataObjects().getCompositeFrameOrCommonFrame().stream()
                .map(JAXBElement::getValue)
                .flatMap(frame -> frame instanceof org.entur.netex2.model.CompositeFrame composite
                        ? composite.getFrames().getCommonFrame().stream().map(JAXBElement::getValue)
                        : Stream.of(frame))
                .filter(MobilityServiceFrame.class::isInstance)
                .map(MobilityServiceFrame.class::cast)
                .filter(frame -> frame.getFleets() != null)
                .flatMap(frame -> frame.getFleets().getFleet().stream())
                .toList();
    }

    private String fixture(String run) throws Exception {
        try (InputStream in = getClass().getResourceAsStream("/fixtures/fleet-import.xml")) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).replace("@RUN@", run);
        }
    }
}

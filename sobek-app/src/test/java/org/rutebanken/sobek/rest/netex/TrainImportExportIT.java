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
import org.entur.netex2.model.CompoundTrain;
import org.entur.netex2.model.PublicationDeliveryStructure;
import org.entur.netex2.model.ResourceFrame;
import org.entur.netex2.model.Train;
import org.entur.netex2.model.TrainComponent;
import org.entur.netex2.model.TrainElementType_VersionStructure;
import org.entur.netex2.model.VehicleType_VersionStructure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rutebanken.sobek.repository.VehicleTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Import and export of NeTEx Train and CompoundTrain through the REST API, with schema validation on in both directions.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(SingleOrganisationAuthorizationConfiguration.class)
class TrainImportExportIT {

    @LocalServerPort int port;

    @Autowired private VehicleTypeRepository vehicleTypeRepository;
    @Autowired private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("Train elements, trains and a compound train that refer to each other are imported, in dependency order, and exported again")
    void importThenExportKeepsTypesOrderAndReferences() throws Exception {
        String run = String.valueOf(System.nanoTime());
        String trainA = "AKT:Train:run" + run + "a";
        String trainB = "AKT:Train:run" + run + "b";
        String compound = "AKT:CompoundTrain:run" + run + "c";
        String front = "AKT:TrainElement:run" + run + "front";
        String rear = "AKT:TrainElement:run" + run + "rear";
        String single = "AKT:TrainElement:run" + run + "single";

        // When: the compound train is first in the file, before the trains it refers to, and the train elements are last
        String importResponse = given()
                .contentType(ContentType.XML)
                .body(fixture("/fixtures/train-import.xml").replace("@RUN@", run))
                .when()
                .post("/services/vehicles/netex?vehicleTypeKind=PHYSICAL")
                .then()
                .statusCode(200)
                .extract().body().asString();

        // Then: the response has all of them, as the right types
        List<VehicleType_VersionStructure> imported = transportTypes(importResponse);
        assertThat(imported).hasSize(3);
        assertThat(imported).extracting(VehicleType_VersionStructure::getId).containsExactlyInAnyOrder(trainA, trainB, compound);
        assertThat(trainElements(importResponse)).extracting(TrainElementType_VersionStructure::getId).containsExactlyInAnyOrder(front, rear, single);
        assertThat(find(imported, trainA)).isInstanceOf(Train.class);
        assertThat(find(imported, compound)).isInstanceOf(CompoundTrain.class);

        // And: the database has them as Train and CompoundTrain, and the components keep the ids the client gave them
        transactionTemplate.executeWithoutResult(status -> {
            var storedTrain = vehicleTypeRepository.findFirstByNetexIdOrderByVersionDesc(trainA);
            assertThat(storedTrain).isInstanceOf(org.rutebanken.sobek.model.vehicle.Train.class);
            var components = ((org.rutebanken.sobek.model.vehicle.Train) storedTrain).getComponents();
            assertThat(components).extracting(c -> c.getLabel().getValue()).containsExactly("first car", "second car");
            assertThat(components).extracting(c -> c.getTrainElement().getNetexId()).containsExactly(front, rear);
            assertThat(components.getFirst().getTrainElement()).isInstanceOf(org.rutebanken.sobek.model.vehicle.TrainElement.class);
            assertThat(components).extracting(org.rutebanken.sobek.model.vehicle.TrainComponent::getNetexId)
                    .containsExactly("AKT:TrainComponent:run" + run + "a1", "AKT:TrainComponent:run" + run + "a2");

            var storedCompound = (org.rutebanken.sobek.model.vehicle.CompoundTrain) vehicleTypeRepository.findFirstByNetexIdOrderByVersionDesc(compound);
            assertThat(storedCompound.getComponents()).extracting(c -> c.getTrain().getNetexId()).containsExactly(trainA, trainB);
        });

        // And: the export gives them back, with the schema validated, components in order and the references kept
        String exportXml = given()
                .accept(ContentType.XML)
                .queryParam("dataOwner", SingleOrganisationAuthorizationConfiguration.DATA_OWNER)
                .queryParam("vehicleTypeKind", "PHYSICAL")
                .when()
                .get("/services/vehicles/netex")
                .then()
                .statusCode(200)
                .extract().body().asString();

        List<VehicleType_VersionStructure> exported = transportTypes(exportXml);
        assertThat(trainElements(exportXml)).extracting(TrainElementType_VersionStructure::getId).contains(front, rear, single);
        org.entur.netex2.model.TrainElement exportedFront = (org.entur.netex2.model.TrainElement) trainElements(exportXml).stream()
                .filter(e -> front.equals(e.getId())).findFirst().orElseThrow();
        assertThat(exportedFront.getTrainElementType()).isEqualTo(org.entur.netex2.model.TrainElementTypeTypeEnumeration.ENGINE);
        Train exportedA = (Train) find(exported, trainA);
        assertThat(exportedA.getTrainSize().getNumberOfCars().intValue()).isEqualTo(2);
        var exportedComponents = exportedA.getComponents().getTrainComponentRefOrTrainComponent().stream().map(TrainComponent.class::cast).toList();
        assertThat(exportedComponents).extracting(c -> c.getTrainElementRef().getRef())
                .containsExactly(front, rear);
        assertThat(exportedComponents).extracting(c -> c.getOrder().intValue()).containsExactly(1, 2);
        assertThat(exportedComponents).extracting(TrainComponent::getId)
                .containsExactly("AKT:TrainComponent:run" + run + "a1", "AKT:TrainComponent:run" + run + "a2");
        assertThat(exportedComponents.getFirst().getForwardCoupling().getThroughAccess())
                .isEqualTo(org.entur.netex2.model.ThroughAccessEnumeration.DOOR);

        CompoundTrain exportedCompound = (CompoundTrain) find(exported, compound);
        assertThat(exportedCompound.getComponents().getTrainInCompoundTrain())
                .extracting(c -> c.getTrainRef().getValue().getRef())
                .containsExactly(trainA, trainB);
    }

    @Test
    @DisplayName("A compound train that refers to a train that does not exist is rejected")
    void compoundTrainReferringToUnknownTrainIsBadRequest() throws Exception {
        String run = String.valueOf(System.nanoTime());
        String xml = fixture("/fixtures/train-import.xml").replace("@RUN@", run)
                .replace("AKT:Train:run" + run + "b", "AKT:Train:missing" + run);
        // Remove the train the compound refers to, so that only train A is in the file
        xml = xml.replaceAll("(?s)<Train id=\"AKT:Train:missing" + run + "\".*?</Train>", "");

        String body = given()
                .contentType(ContentType.XML)
                .body(xml)
                .when()
                .post("/services/vehicles/netex?vehicleTypeKind=PHYSICAL")
                .then()
                .statusCode(400)
                .extract().body().asString();

        assertThat(body).contains("AKT:Train:missing" + run);
    }

    @Test
    @DisplayName("A train that refers to a train element that does not exist is rejected, and nothing of the file is saved")
    void trainReferringToUnknownElementIsBadRequest() throws Exception {
        String run = String.valueOf(System.nanoTime());
        // The elements of the fixture are removed, so that the trains refer to elements that are in neither the file nor Sobek
        String xml = fixture("/fixtures/train-import.xml").replace("@RUN@", run)
                .replaceAll("(?s)<trainElementTypes>.*?</trainElementTypes>", "");

        String body = given()
                .contentType(ContentType.XML)
                .body(xml)
                .when()
                .post("/services/vehicles/netex?vehicleTypeKind=PHYSICAL")
                .then()
                .statusCode(400)
                .extract().body().asString();

        assertThat(body).contains("AKT:TrainElement:run" + run).contains("not found");
        transactionTemplate.executeWithoutResult(status ->
                assertThat(vehicleTypeRepository.findFirstByNetexIdOrderByVersionDesc("AKT:Train:run" + run + "a")).isNull());
    }

    @Test
    @DisplayName("Train element types other than TrainElement are rejected")
    void trailingElementTypeIsBadRequest() throws Exception {
        String run = String.valueOf(System.nanoTime());
        // A trailing element type (not a TrainElement) in the frame, in place of the elements of the fixture
        String xml = fixture("/fixtures/train-import.xml").replace("@RUN@", run)
                .replaceAll("(?s)<trainElementTypes>.*?</trainElementTypes>",
                        "<trainElementTypes><TrailingElementType id=\"AKT:TrailingElementType:run" + run + "\" version=\"1\">"
                                + "<Name lang=\"nb\"><Text>Trailing</Text></Name></TrailingElementType></trainElementTypes>");

        String body = given()
                .contentType(ContentType.XML)
                .body(xml)
                .when()
                .post("/services/vehicles/netex?vehicleTypeKind=PHYSICAL")
                .then()
                .statusCode(400)
                .extract().body().asString();

        assertThat(body).contains("TrailingElementType");
    }

    @Test
    @DisplayName("Trains that refer to each other in a cycle are rejected")
    void cycleIsBadRequest() throws Exception {
        String run = String.valueOf(System.nanoTime());
        // Two compound trains that each name the other in a TrainRef. (The import only orders by what is in the file;
        // that neither can be saved first is what is rejected here. Sobek would also reject them, as neither is a Train.)
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <PublicationDelivery xmlns="http://www.netex.org.uk/netex" version="1.0">
                  <PublicationTimestamp>2026-03-16T10:25:00.619Z</PublicationTimestamp>
                  <ParticipantRef>TST</ParticipantRef>
                  <dataObjects>
                    <ResourceFrame id="RF:1" version="1">
                      <FrameDefaults>
                        <DefaultResponsibilitySetRef ref="TST:ResponsibilitySet:1" />
                        <DefaultLocale><TimeZone>Europe/Oslo</TimeZone></DefaultLocale>
                      </FrameDefaults>
                      <responsibilitySets>
                        <ResponsibilitySet id="TST:ResponsibilitySet:1" version="1">
                          <roles>
                            <ResponsibilityRoleAssignment id="TST:ResponsibilityRoleAssignment:1" version="1">
                              <DataRoleType>owns</DataRoleType>
                              <ResponsibleOrganisationRef ref="AKT"/>
                            </ResponsibilityRoleAssignment>
                          </roles>
                        </ResponsibilitySet>
                      </responsibilitySets>
                      <vehicleTypes>
                        <CompoundTrain id="AKT:CompoundTrain:run@RUN@x" version="1">
                          <components><TrainInCompoundTrain id="AKT:TrainInCompoundTrain:run@RUN@x1" version="1"><TrainRef ref="AKT:CompoundTrain:run@RUN@y"/></TrainInCompoundTrain></components>
                        </CompoundTrain>
                        <CompoundTrain id="AKT:CompoundTrain:run@RUN@y" version="1">
                          <components><TrainInCompoundTrain id="AKT:TrainInCompoundTrain:run@RUN@y1" version="1"><TrainRef ref="AKT:CompoundTrain:run@RUN@x"/></TrainInCompoundTrain></components>
                        </CompoundTrain>
                      </vehicleTypes>
                    </ResourceFrame>
                  </dataObjects>
                </PublicationDelivery>
                """.replace("@RUN@", run);

        String body = given()
                .contentType(ContentType.XML)
                .body(xml)
                .when()
                .post("/services/vehicles/netex?vehicleTypeKind=PHYSICAL")
                .then()
                .statusCode(400)
                .extract().body().asString();

        assertThat(body).contains("cycle");
    }

    private static List<TrainElementType_VersionStructure> trainElements(String xml) throws Exception {
        Unmarshaller unmarshaller = JAXBContext.newInstance(PublicationDeliveryStructure.class).createUnmarshaller();
        @SuppressWarnings("unchecked")
        PublicationDeliveryStructure delivery = ((JAXBElement<PublicationDeliveryStructure>) unmarshaller.unmarshal(new StringReader(xml))).getValue();
        return delivery.getDataObjects().getCompositeFrameOrCommonFrame().stream()
                .map(JAXBElement::getValue)
                .flatMap(frame -> frame instanceof org.entur.netex2.model.CompositeFrame composite
                        ? composite.getFrames().getCommonFrame().stream().map(JAXBElement::getValue)
                        : java.util.stream.Stream.of(frame))
                .filter(ResourceFrame.class::isInstance)
                .map(ResourceFrame.class::cast)
                .filter(frame -> frame.getTrainElementTypes() != null)
                .flatMap(frame -> frame.getTrainElementTypes().getTrailingElementTypeOrTractiveElementTypeOrTrainElement().stream())
                .toList();
    }

    private static VehicleType_VersionStructure find(List<VehicleType_VersionStructure> types, String id) {
        return types.stream().filter(t -> id.equals(t.getId())).findFirst().orElseThrow(() -> new AssertionError("Not in the delivery: " + id));
    }

    private static List<VehicleType_VersionStructure> transportTypes(String xml) throws Exception {
        Unmarshaller unmarshaller = JAXBContext.newInstance(PublicationDeliveryStructure.class).createUnmarshaller();
        @SuppressWarnings("unchecked")
        PublicationDeliveryStructure delivery = ((JAXBElement<PublicationDeliveryStructure>) unmarshaller.unmarshal(new StringReader(xml))).getValue();
        return delivery.getDataObjects().getCompositeFrameOrCommonFrame().stream()
                .map(JAXBElement::getValue)
                .flatMap(frame -> frame instanceof org.entur.netex2.model.CompositeFrame composite
                        ? composite.getFrames().getCommonFrame().stream().map(JAXBElement::getValue)
                        : java.util.stream.Stream.of(frame))
                .filter(ResourceFrame.class::isInstance)
                .map(ResourceFrame.class::cast)
                .filter(frame -> frame.getVehicleTypes() != null)
                .flatMap(frame -> frame.getVehicleTypes().getTransportType().stream())
                .map(JAXBElement::getValue)
                .map(VehicleType_VersionStructure.class::cast)
                .toList();
    }

    private String fixture(String path) throws Exception {
        try (InputStream in = getClass().getResourceAsStream(path)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}

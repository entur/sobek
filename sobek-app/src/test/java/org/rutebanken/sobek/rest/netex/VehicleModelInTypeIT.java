package org.rutebanken.sobek.rest.netex;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rutebanken.sobek.netex.id.VehicleModelId;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

/**
 * Sobek keeps no vehicle model of its own: the model is kept in the vehicle type, read from the NeTEx vehicle model on
 * import and derived from the type on export. A model that does not belong to exactly one type is rejected.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(SingleOrganisationAuthorizationConfiguration.class)
class VehicleModelInTypeIT {

    private static final String DATA_OWNER = SingleOrganisationAuthorizationConfiguration.DATA_OWNER;

    @LocalServerPort int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("The vehicle model of an imported type is kept in the type, and exported with an id derived from it")
    void modelIsKeptInTheTypeAndExportedFromIt() {
        String suffix = unique();
        String typeId = "EXT:VehicleType:" + suffix;
        String modelId = "EXT:VehicleModel:" + suffix;

        String response = postImport(delivery(vehicleType(typeId, null), models(model(modelId, typeId, "Stadler", "500", "120")), ""))
                .statusCode(200).extract().body().asString();

        String assignedTypeId = firstMatch(response, "<VehicleType[^>]* id=\"([^\"]+)\"");
        String derivedModelId = VehicleModelId.forVehicleType(assignedTypeId);

        // The response has the model its type is classified as, also when no vehicle is imported
        assertThat(response).contains("<VehicleModel ").contains("id=\"" + derivedModelId + "\"").contains("Stadler");

        String export = export();
        // The model is an element of its own, with the id derived from the id of the type, and the type is classified as it
        assertThat(export).contains("id=\"" + derivedModelId + "\"");
        assertThat(export).contains("Stadler");
        assertThat(export).contains("<ClassifiedAsRef ref=\"" + derivedModelId + "\"");
        // The id the client sent for the model is not kept
        assertThat(export).doesNotContain(modelId);
    }

    @Test
    @DisplayName("A type can be classified as its model instead of the model referring to the type")
    void typeClassifiedAsModelIsAccepted() {
        String suffix = unique();
        String typeId = "EXT:VehicleType:" + suffix;
        String modelId = "EXT:VehicleModel:" + suffix;

        postImport(delivery(vehicleType(typeId, modelId), models(model(modelId, null, "Siemens", null, null)), ""))
                .statusCode(200);
    }

    @Test
    @DisplayName("A vehicle model that does not belong to a vehicle type is rejected")
    void orphanModelIsRejected() {
        String suffix = unique();
        String body = postImport(delivery(vehicleType("EXT:VehicleType:" + suffix, null), models(model("EXT:VehicleModel:" + suffix, null, "Stadler", "500", null)), ""))
                .statusCode(400).extract().body().asString();

        assertThat(body).contains("EXT:VehicleModel:" + suffix).contains("does not belong to a vehicle type");
    }

    @Test
    @DisplayName("A vehicle model that refers to a vehicle type that is not in the delivery is rejected")
    void modelOfATypeOutsideTheDeliveryIsRejected() {
        String suffix = unique();
        String body = postImport(delivery(vehicleType("EXT:VehicleType:" + suffix, null), models(model("EXT:VehicleModel:" + suffix, "EXT:VehicleType:other-" + suffix, "Stadler", "500", null)), ""))
                .statusCode(400).extract().body().asString();

        assertThat(body).contains("other-" + suffix);
    }

    @Test
    @DisplayName("A vehicle model shared by more than one vehicle type is rejected")
    void modelSharedByTypesIsRejected() {
        String suffix = unique();
        String modelId = "EXT:VehicleModel:" + suffix;
        String body = postImport(delivery(vehicleType("EXT:VehicleType:a-" + suffix, modelId) + vehicleType("EXT:VehicleType:b-" + suffix, modelId), models(model(modelId, null, "Stadler", "500", null)), ""))
                .statusCode(400).extract().body().asString();

        assertThat(body).contains("more than one vehicle type");
    }

    @Test
    @DisplayName("A vehicle type with more than one vehicle model is rejected")
    void typeWithMoreThanOneModelIsRejected() {
        String suffix = unique();
        String typeId = "EXT:VehicleType:" + suffix;
        String body = postImport(delivery(vehicleType(typeId, null), models(model("EXT:VehicleModel:a-" + suffix, typeId, "Stadler", "500", null)
                + model("EXT:VehicleModel:b-" + suffix, typeId, "Siemens", "300", null)), ""))
                .statusCode(400).extract().body().asString();

        assertThat(body).contains("more than one vehicle model");
    }

    @Test
    @DisplayName("A vehicle that refers to a vehicle model its type does not have is rejected")
    void vehicleWithModelRefOfATypeWithoutModelIsRejected() {
        String suffix = unique();
        String typeId = "EXT:VehicleType:" + suffix;
        String body = postImport(delivery(vehicleType(typeId, null), "",
                vehicle("EXT:Vehicle:" + suffix, typeId, "EXT:VehicleModel:" + suffix)))
                .statusCode(400).extract().body().asString();

        assertThat(body).contains("has no vehicle model");
    }

    @Test
    @DisplayName("A vehicle that refers to another vehicle model than the one of its type is rejected")
    void vehicleWithModelRefOfAnotherModelIsRejected() {
        String suffix = unique();
        String typeId = "EXT:VehicleType:" + suffix;
        String modelId = "EXT:VehicleModel:" + suffix;
        String body = postImport(delivery(vehicleType(typeId, null), models(model(modelId, typeId, "Stadler", "500", null)),
                vehicle("EXT:Vehicle:" + suffix, typeId, "EXT:VehicleModel:other-" + suffix)))
                .statusCode(400).extract().body().asString();

        assertThat(body).contains("not the vehicle model of its vehicle type");
    }

    @Test
    @DisplayName("A vehicle that refers to the vehicle model of its type is accepted")
    void vehicleWithTheModelOfItsTypeIsAccepted() {
        String suffix = unique();
        String typeId = "EXT:VehicleType:" + suffix;
        String modelId = "EXT:VehicleModel:" + suffix;

        postImport(delivery(vehicleType(typeId, null), models(model(modelId, typeId, "Stadler", "500", null)),
                vehicle("EXT:Vehicle:" + suffix, typeId, modelId)))
                .statusCode(200);
    }

    // --- NeTEx ---

    private static String unique() {
        return String.valueOf(System.nanoTime());
    }

    private static String vehicleType(String id, String classifiedAsModelId) {
        return "<VehicleType version=\"1\" id=\"" + id + "\">"
                + "<Name>Model test type</Name>"
                + "<TransportMode>rail</TransportMode>"
                + (classifiedAsModelId == null ? "" : "<ClassifiedAsRef ref=\"" + classifiedAsModelId + "\"/>")
                + "</VehicleType>";
    }

    private static String model(String id, String typeId, String manufacturer, String range, String fullCharge) {
        return "<VehicleModel version=\"1\" id=\"" + id + "\">"
                + "<Manufacturer>" + manufacturer + "</Manufacturer>"
                + (typeId == null ? "" : "<TransportTypeRef ref=\"" + typeId + "\"/>")
                + (range == null ? "" : "<Range>" + range + "</Range>")
                + (fullCharge == null ? "" : "<FullCharge>" + fullCharge + "</FullCharge>")
                + "</VehicleModel>";
    }

    private static String models(String models) {
        return "<vehicleModels>" + models + "</vehicleModels>";
    }

    private static String vehicle(String id, String typeId, String modelId) {
        return "<Vehicle version=\"1\" id=\"" + id + "\">"
                + "<ValidBetween><FromDate>2026-04-17T00:00:00</FromDate></ValidBetween>"
                + "<Name lang=\"nb\">Model test vehicle</Name>"
                + "<RegistrationNumber>MOD-" + unique() + "</RegistrationNumber>"
                + "<TransportTypeRef ref=\"" + typeId + "\"/>"
                + "<VehicleModelRef ref=\"" + modelId + "\"/>"
                + "</Vehicle>";
    }

    /** A delivery with the given vehicle types, vehicle models (see {@link #models}) and vehicles. */
    private static String delivery(String types, String models, String vehicles) {
        return """
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
                              <ResponsibleOrganisationRef ref="%s"/>
                            </ResponsibilityRoleAssignment>
                          </roles>
                        </ResponsibilitySet>
                      </responsibilitySets>
                      <vehicleTypes>%s</vehicleTypes>
                      %s
                      %s
                    </ResourceFrame>
                  </dataObjects>
                </PublicationDelivery>
                """.formatted(DATA_OWNER, types, models, vehicles.isEmpty() ? "" : "<vehicles>" + vehicles + "</vehicles>");
    }

    private static ValidatableResponse postImport(String body) {
        return given()
                .contentType("application/xml")
                .body(body)
                .when()
                .post("/services/vehicles/netex?vehicleTypeKind=PHYSICAL")
                .then();
    }

    private static String export() {
        return given()
                .accept("application/xml")
                .queryParam("dataOwner", DATA_OWNER)
                .queryParam("vehicleTypeKind", "PHYSICAL")
                .when()
                .get("/services/vehicles/netex")
                .then()
                .statusCode(200)
                .extract().body().asString();
    }

    private static String firstMatch(String text, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(text);
        assertThat(matcher.find()).as("%s in %s", regex, text).isTrue();
        return matcher.group(1);
    }
}

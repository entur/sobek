package org.rutebanken.sobek.rest.graphql;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

/**
 * What NeTEx keeps in a vehicle model (manufacturer and full charge, in GraphQL) is kept in the vehicle type, and is read and
 * written through the vehicle type in GraphQL.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class VehicleTypeModelGraphQLTest {

    @LocalServerPort
    int port;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("Manufacturer and full charge are written to a vehicle type and read back, and kept by a new version")
    void modelFieldsAreWrittenAndReadBack() {
        String id = createVehicleType("manufacturer: { value: \"Stadler\" lang: \"en\" } fullCharge: 120");

        read(id)
                .body("errors", nullValue())
                .body("data.vehicleTypes.content[0].manufacturer.value", equalTo("Stadler"))
                .body("data.vehicleTypes.content[0].manufacturer.lang", equalTo("en"))
                .body("data.vehicleTypes.content[0].fullCharge", equalTo(120f));

        // A new version replaces the fields: what is left out is removed, as for the other fields of the vehicle type
        graphql("mutation { createOrUpdateVehicleType(input: { netexId: \"" + id + "\" name: { value: \"Model test\" lang: \"en\" } transportMode: BUS "
                + "manufacturer: { value: \"Siemens\" lang: \"en\" } dataOwnerRef: \"AKT\" }) }")
                .body("errors", nullValue());
        read(id)
                .body("data.vehicleTypes.content[0].manufacturer.value", equalTo("Siemens"))
                .body("data.vehicleTypes.content[0].fullCharge", nullValue());
    }

    @Test
    @DisplayName("The range, which is not in GraphQL, is kept by a new version made through GraphQL")
    void rangeIsKeptByANewVersion() {
        String id = createVehicleType("fullCharge: 120");
        jdbc.update("UPDATE vehicle_type SET range = 500 WHERE netex_id = ?", id);

        graphql("mutation { createOrUpdateVehicleType(input: { netexId: \"" + id + "\" name: { value: \"Model test\" lang: \"en\" } transportMode: BUS "
                + "fullCharge: 130 dataOwnerRef: \"AKT\" }) }")
                .body("errors", nullValue());

        assertThat(jdbc.queryForObject("SELECT range FROM vehicle_type WHERE netex_id = ? ORDER BY version DESC LIMIT 1", java.math.BigDecimal.class, id))
                .isEqualByComparingTo("500");
    }

    @Test
    @DisplayName("A vehicle type without model data has none")
    void noModelData() {
        read(createVehicleType(""))
                .body("data.vehicleTypes.content[0].manufacturer", nullValue())
                .body("data.vehicleTypes.content[0].fullCharge", nullValue());
    }

    @Test
    @DisplayName("A train element has no vehicle model: its model fields are rejected")
    void trainElementCannotHaveModelFields() {
        for (String field : new String[]{"manufacturer: { value: \"Stadler\" lang: \"en\" }", "fullCharge: 10"}) {
            graphql(createMutation("subtype: TRAIN_ELEMENT trainElementType: CARRIAGE " + field))
                    .body("errors[0].extensions.classification", equalTo("BAD_REQUEST"))
                    .body("errors[0].message", allOf(containsString(field.substring(0, field.indexOf(':'))), containsString("TRAIN_ELEMENT")));
        }
    }

    private String createVehicleType(String fields) {
        return graphql(createMutation(fields))
                .body("errors", nullValue())
                .extract().path("data.createOrUpdateVehicleType");
    }

    private static String createMutation(String fields) {
        return "mutation { createOrUpdateVehicleType(input: { name: { value: \"Model test\" lang: \"en\" } transportMode: BUS vehicleTypeKind: PHYSICAL "
                + fields + " dataOwnerRef: \"AKT\" }) }";
    }

    private ValidatableResponse read(String netexId) {
        return graphql("{ vehicleTypes(filter: { netexIds: [\"" + netexId + "\"] dataOwnerRef: \"AKT\" vehicleTypeKind: PHYSICAL }) "
                + "{ content { manufacturer { value lang } fullCharge } } }");
    }

    private ValidatableResponse graphql(String query) {
        return given()
                .contentType(ContentType.JSON)
                .body("{\"query\": \"" + query.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"}")
                .when()
                .post("/services/vehicles/graphql")
                .then()
                .log().ifValidationFails()
                .statusCode(200);
    }
}

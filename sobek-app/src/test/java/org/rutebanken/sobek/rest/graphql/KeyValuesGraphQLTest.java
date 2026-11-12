package org.rutebanken.sobek.rest.graphql;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.nullValue;

/** keyValues through GraphQL for vehicle types, vehicles and deck plans: written, kept between versions, and read back (also nested). */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class KeyValuesGraphQLTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void createWithKeyValues() {
        graphql("mutation { createOrUpdateVehicleType(input: { name: { value: \"KV\" lang: \"en\" } transportMode: BUS "
                + "vehicleTypeKind: PHYSICAL dataOwnerRef: \"AKT\" "
                + "keyValues: [{ key: \"imported-id\" value: \"AUTOSYS:VehicleType:TEST\" }, { key: \"MaximumEngineEffectKW\" value: \"265\" }] }) }")
                .body("errors", nullValue());
    }

    @Test
    void updateWithKeyValues() {
        String id = graphql("mutation { createOrUpdateVehicleType(input: { name: { value: \"KV\" lang: \"en\" } transportMode: BUS "
                + "vehicleTypeKind: PHYSICAL dataOwnerRef: \"AKT\" }) }")
                .body("errors", nullValue())
                .extract().path("data.createOrUpdateVehicleType");
        graphql("mutation { createOrUpdateVehicleType(input: { netexId: \"" + id + "\" name: { value: \"KV2\" lang: \"en\" } transportMode: BUS "
                + "dataOwnerRef: \"AKT\" keyValues: [{ key: \"imported-id\" value: \"AUTOSYS:VehicleType:TEST\" }] }) }")
                .body("errors", nullValue());
    }

    @Test
    void roundTripKeyValues() {
        String kv = "keyValues: [{ key: \"imported-id\" value: \"AUTOSYS:VehicleType:TEST\" }, { key: \"MaximumEngineEffectKW\" value: \"265\" }]";
        String id = graphql("mutation { createOrUpdateVehicleType(input: { name: { value: \"KV\" lang: \"en\" } transportMode: BUS "
                + "vehicleTypeKind: PHYSICAL dataOwnerRef: \"AKT\" " + kv + " }) }")
                .body("errors", nullValue())
                .extract().path("data.createOrUpdateVehicleType");
        graphql("mutation { createOrUpdateVehicleType(input: { netexId: \"" + id + "\" name: { value: \"KV2\" lang: \"en\" } transportMode: BUS "
                + "dataOwnerRef: \"AKT\" " + kv + " }) }")
                .body("errors", nullValue());
        graphql("{ vehicleTypes(filter: { netexIds: [\"" + id + "\"] dataOwnerRef: \"AKT\" vehicleTypeKind: PHYSICAL }) "
                + "{ content { version keyValues { key value } } } }")
                .body("errors", nullValue())
                .body("data.vehicleTypes.content[0].keyValues.key", org.hamcrest.Matchers.containsInAnyOrder("imported-id", "MaximumEngineEffectKW"))
                .body("data.vehicleTypes.content[0].keyValues.value", org.hamcrest.Matchers.containsInAnyOrder("AUTOSYS:VehicleType:TEST", "265"));
    }

    @Test
    void vehicleKeyValuesRoundTrip() {
        keyValuesRoundTrip("Vehicle", "vehicles", "registrationNumber: \"KV-1\"");
    }

    @Test
    void deckPlanKeyValuesRoundTrip() {
        keyValuesRoundTrip("DeckPlan", "deckPlans", "name: { value: \"KV deck plan\" lang: \"en\" }");
    }

    @Test
    void nestedKeyValues() {
        String kv = "keyValues: [{ key: \"k\" value: \"v\" }]";
        String deckPlan = graphql("mutation { createOrUpdateDeckPlan(input: { name: { value: \"N\" lang: \"en\" } dataOwnerRef: \"AKT\" " + kv + " }) }")
                .body("errors", nullValue()).extract().path("data.createOrUpdateDeckPlan");
        String vt = graphql("mutation { createOrUpdateVehicleType(input: { name: { value: \"N\" lang: \"en\" } transportMode: BUS vehicleTypeKind: PHYSICAL "
                + "dataOwnerRef: \"AKT\" deckPlan: { netexId: \"" + deckPlan + "\" } " + kv + " }) }")
                .body("errors", nullValue()).extract().path("data.createOrUpdateVehicleType");
        String vehicle = graphql("mutation { createOrUpdateVehicle(input: { registrationNumber: \"NEST-1\" transportType: { netexId: \"" + vt + "\" } dataOwnerRef: \"AKT\" " + kv + " }) }")
                .body("errors", nullValue()).extract().path("data.createOrUpdateVehicle");
        graphql("{ vehicles(filter: { netexIds: [\"" + vehicle + "\"] dataOwnerRef: \"AKT\" }) { content { keyValues { key } transportType { keyValues { key } } } } }")
                .body("errors", nullValue())
                .body("data.vehicles.content[0].transportType.keyValues.key", org.hamcrest.Matchers.contains("k"));
        graphql("{ vehicleTypes(filter: { netexIds: [\"" + vt + "\"] dataOwnerRef: \"AKT\" vehicleTypeKind: PHYSICAL }) { content { deckPlan { keyValues { key } } vehicles { keyValues { key } } } } }")
                .body("errors", nullValue())
                .body("data.vehicleTypes.content[0].deckPlan.keyValues.key", org.hamcrest.Matchers.contains("k"))
                .body("data.vehicleTypes.content[0].vehicles[0].keyValues.key", org.hamcrest.Matchers.contains("k"));
    }

    private void keyValuesRoundTrip(String entity, String query, String fields) {
        String mutation = "createOrUpdate" + entity;
        String kv = "keyValues: [{ key: \"imported-id\" value: \"AUTOSYS:" + entity + ":TEST\" }, { key: \"other\" value: \"1\" }]";
        String id = graphql("mutation { " + mutation + "(input: { " + fields + " dataOwnerRef: \"AKT\" " + kv + " }) }")
                .body("errors", nullValue())
                .extract().path("data." + mutation);
        // updated without key values: the ones of the previous version are kept
        graphql("mutation { " + mutation + "(input: { netexId: \"" + id + "\" " + fields + " dataOwnerRef: \"AKT\" }) }")
                .body("errors", nullValue());
        // updated with one of them changed: that one is replaced, the other is kept
        graphql("mutation { " + mutation + "(input: { netexId: \"" + id + "\" " + fields + " dataOwnerRef: \"AKT\" "
                + "keyValues: [{ key: \"other\" value: \"2\" }] }) }")
                .body("errors", nullValue());
        graphql("{ " + query + "(filter: { netexIds: [\"" + id + "\"] dataOwnerRef: \"AKT\" }) { content { keyValues { key value } } } }")
                .body("errors", nullValue())
                .body("data." + query + ".content[0].keyValues.key", org.hamcrest.Matchers.containsInAnyOrder("imported-id", "other"))
                .body("data." + query + ".content[0].keyValues.value", org.hamcrest.Matchers.containsInAnyOrder("AUTOSYS:" + entity + ":TEST", "2"));
    }

    private ValidatableResponse graphql(String query) {
        return given().contentType(ContentType.JSON)
                .body(java.util.Map.of("query", query))
                .when().post("/services/vehicles/graphql")
                .then().log().ifValidationFails().statusCode(200);
    }
}

package org.rutebanken.sobek.rest.graphql;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * Fleets through GraphQL: createOrUpdateFleet, fleets and deactivateFleet. The members and the transport types are
 * given as references to vehicles and vehicle types that exist, and replace those of the previous version.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class FleetGraphQLTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("A fleet is created with its members and transport types, and read back")
    void fleetIsCreatedAndReadBack() {
        String type = createVehicleType("AKT");
        String first = createVehicle("AKT", type, "First bus");
        String second = createVehicle("AKT", type, "Second bus");

        String fleetId = createFleet("AKT", "City buses", "members: [ { netexId: \"" + first + "\" }, { netexId: \"" + second + "\" } ] "
                + "transportTypes: [ { netexId: \"" + type + "\" } ] "
                + "shortName: { value: \"CB\" lang: \"en\" } privateCode: { type: \"depot\" value: \"CB-1\" } "
                + "keyValues: [ { key: \"garage\" value: \"North\" } ]");

        readFleet(fleetId, "AKT")
                .body("errors", nullValue())
                .body("data.fleets.totalElements", equalTo(1))
                .body("data.fleets.content[0].netexId", equalTo(fleetId))
                .body("data.fleets.content[0].version", equalTo("1"))
                .body("data.fleets.content[0].dataOwnerRef", equalTo("AKT"))
                .body("data.fleets.content[0].name.value", equalTo("City buses"))
                .body("data.fleets.content[0].shortName.value", equalTo("CB"))
                .body("data.fleets.content[0].privateCode.value", equalTo("CB-1"))
                .body("data.fleets.content[0].privateCode.type", equalTo("depot"))
                .body("data.fleets.content[0].keyValues[0].key", equalTo("garage"))
                .body("data.fleets.content[0].keyValues[0].value", equalTo("North"))
                .body("data.fleets.content[0].members.netexId", org.hamcrest.Matchers.containsInAnyOrder(first, second))
                .body("data.fleets.content[0].members.name.value", org.hamcrest.Matchers.containsInAnyOrder("First bus", "Second bus"))
                .body("data.fleets.content[0].transportTypes.netexId", org.hamcrest.Matchers.contains(type));
    }

    @Test
    @DisplayName("Updating a fleet makes a new version, and the members are the ones of the new input")
    void updateReplacesTheMembers() {
        String type = createVehicleType("AKT");
        String first = createVehicle("AKT", type, "Kept bus");
        String second = createVehicle("AKT", type, "Dropped bus");
        String fleetId = createFleet("AKT", "Shrinking fleet", "members: [ { netexId: \"" + first + "\" }, { netexId: \"" + second + "\" } ]");

        graphql("mutation { createOrUpdateFleet(input: { netexId: \"" + fleetId + "\" name: { value: \"Shrinking fleet\" lang: \"en\" } "
                + "members: [ { netexId: \"" + first + "\" } ] dataOwnerRef: \"AKT\" }) }")
                .body("errors", nullValue())
                .body("data.createOrUpdateFleet", equalTo(fleetId));

        readFleet(fleetId, "AKT")
                .body("data.fleets.content[0].version", equalTo("2"))
                .body("data.fleets.content[0].members.netexId", org.hamcrest.Matchers.contains(first))
                .body("data.fleets.content[0].transportTypes", org.hamcrest.Matchers.empty());
    }

    @Test
    @DisplayName("Fleets are found by name, and the fleets of a data owner without access cannot be read")
    void fleetsAreFilteredByNameAndDataOwner() {
        String fleetId = createFleet("AKT", "Findable fleet xyz", "");

        graphql("{ fleets(filter: { name: \"findable FLEET xyz\" dataOwnerRef: \"AKT\" }) { totalElements content { netexId } } }")
                .body("errors", nullValue())
                .body("data.fleets.totalElements", equalTo(1))
                .body("data.fleets.content[0].netexId", equalTo(fleetId));
        // The fleets of a data owner the user has no access to cannot be read
        graphql("{ fleets(filter: { name: \"findable fleet xyz\" dataOwnerRef: \"RUT\" }) { totalElements } }")
                .body("errors[0].extensions.classification", equalTo("FORBIDDEN"));
    }

    @Test
    @DisplayName("A fleet with a member or a transport type that does not exist is rejected")
    void unknownReferencesAreRejected() {
        graphql(createMutation("AKT", "Bad fleet", "members: [ { netexId: \"AKT:Vehicle:does-not-exist\" } ]"))
                .body("errors[0].extensions.classification", equalTo("BAD_REQUEST"))
                .body("errors[0].message", containsString("AKT:Vehicle:does-not-exist"));

        graphql(createMutation("AKT", "Bad fleet", "transportTypes: [ { netexId: \"AKT:VehicleType:does-not-exist\" } ]"))
                .body("errors[0].extensions.classification", equalTo("BAD_REQUEST"))
                .body("errors[0].message", containsString("AKT:VehicleType:does-not-exist"));
    }

    @Test
    @DisplayName("A vehicle in a fleet cannot be deactivated, and the fleet can be deactivated")
    void deactivation() {
        String type = createVehicleType("AKT");
        String vehicle = createVehicle("AKT", type, "Deactivation bus");
        String fleetId = createFleet("AKT", "Deactivation fleet", "members: [ { netexId: \"" + vehicle + "\" } ]");

        String vehicleVersion = graphql("{ vehicles(filter: { netexIds: [\"" + vehicle + "\"] dataOwnerRef: \"AKT\" }) { content { version } } }")
                .extract().path("data.vehicles.content[0].version");
        graphql("mutation { deactivateVehicle(input: { netexId: \"" + vehicle + "\" version: " + vehicleVersion + " deactivateAt: \"2099-01-01T00:00:00.000+0000\" }) { netexId } }")
                .body("errors[0].extensions.classification", equalTo("BAD_REQUEST"))
                .body("errors[0].message", containsString("member of a fleet"));

        graphql("mutation { deactivateFleet(input: { netexId: \"" + fleetId + "\" version: 1 deactivateAt: \"2099-01-01T00:00:00.000+0000\" }) { netexId version } }")
                .body("errors", nullValue())
                .body("data.deactivateFleet.netexId", equalTo(fleetId))
                .body("data.deactivateFleet.version", equalTo("2"));
    }

    private String createVehicleType(String dataOwner) {
        return graphql("mutation { createOrUpdateVehicleType(input: { name: { value: \"Fleet test type\" lang: \"en\" } transportMode: BUS "
                + "vehicleTypeKind: PHYSICAL dataOwnerRef: \"" + dataOwner + "\" }) }")
                .body("errors", nullValue())
                .extract().path("data.createOrUpdateVehicleType");
    }

    private String createVehicle(String dataOwner, String vehicleType, String name) {
        return graphql("mutation { createOrUpdateVehicle(input: { name: { value: \"" + name + "\" lang: \"en\" } "
                + "transportType: { netexId: \"" + vehicleType + "\" } dataOwnerRef: \"" + dataOwner + "\" }) }")
                .body("errors", nullValue())
                .extract().path("data.createOrUpdateVehicle");
    }

    private String createFleet(String dataOwner, String name, String extraFields) {
        return graphql(createMutation(dataOwner, name, extraFields))
                .body("errors", nullValue())
                .extract().path("data.createOrUpdateFleet");
    }

    private static String createMutation(String dataOwner, String name, String extraFields) {
        return "mutation { createOrUpdateFleet(input: { name: { value: \"" + name + "\" lang: \"en\" } " + extraFields
                + " dataOwnerRef: \"" + dataOwner + "\" }) }";
    }

    private ValidatableResponse readFleet(String fleetId, String dataOwner) {
        return graphql("{ fleets(filter: { netexIds: [\"" + fleetId + "\"] dataOwnerRef: \"" + dataOwner + "\" }) { totalElements content { "
                + "netexId version dataOwnerRef name { value } shortName { value } privateCode { type value } keyValues { key value } "
                + "members { netexId name { value } } transportTypes { netexId } } } }");
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

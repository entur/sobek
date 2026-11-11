package org.rutebanken.sobek.rest.graphql;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import java.util.List;
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

    @Test
    @DisplayName("A vehicle and a vehicle type have the fleets they are in, and none when they are in no fleet")
    void vehiclesAndVehicleTypesHaveTheirFleets() {
        String type = createVehicleType("AKT");
        String otherType = createVehicleType("AKT");
        String inTwo = createVehicle("AKT", type, "In two fleets");
        String inNone = createVehicle("AKT", type, "In no fleet");
        String firstFleet = createFleet("AKT", "First of two", "members: [ { netexId: \"" + inTwo + "\" } ] transportTypes: [ { netexId: \"" + type + "\" } ]");
        String secondFleet = createFleet("AKT", "Second of two", "members: [ { netexId: \"" + inTwo + "\" } ]");

        graphql("{ vehicles(filter: { netexIds: [\"" + inTwo + "\", \"" + inNone + "\"] dataOwnerRef: \"AKT\" }) { content { netexId fleets { netexId name { value } } } } }")
                .body("errors", nullValue())
                .body("data.vehicles.content.find { it.netexId == '" + inTwo + "' }.fleets.netexId",
                        org.hamcrest.Matchers.containsInAnyOrder(firstFleet, secondFleet))
                .body("data.vehicles.content.find { it.netexId == '" + inNone + "' }.fleets", org.hamcrest.Matchers.empty());

        graphql("{ vehicleTypes(filter: { netexIds: [\"" + type + "\", \"" + otherType + "\"] dataOwnerRef: \"AKT\" vehicleTypeKind: PHYSICAL }) { content { netexId fleets { netexId } } } }")
                .body("errors", nullValue())
                .body("data.vehicleTypes.content.find { it.netexId == '" + type + "' }.fleets.netexId", org.hamcrest.Matchers.contains(firstFleet))
                .body("data.vehicleTypes.content.find { it.netexId == '" + otherType + "' }.fleets", org.hamcrest.Matchers.empty());

        // And the other way round: a fleet's members have their fleets too
        graphql("{ fleets(filter: { netexIds: [\"" + firstFleet + "\"] dataOwnerRef: \"AKT\" }) { content { members { fleets { netexId } } transportTypes { fleets { netexId } } } } }")
                .body("errors", nullValue())
                .body("data.fleets.content[0].members[0].fleets.netexId", org.hamcrest.Matchers.containsInAnyOrder(firstFleet, secondFleet))
                .body("data.fleets.content[0].transportTypes[0].fleets.netexId", org.hamcrest.Matchers.contains(firstFleet));
    }

    @Test
    @DisplayName("Vehicles and vehicle types are added to and removed from a fleet in one new version, and the fleet is otherwise unchanged")
    void vehiclesAndTransportTypesAreAddedAndRemoved() {
        String type = createVehicleType("AKT");
        String addedType = createVehicleType("AKT");
        String neverInFleet = createVehicleType("AKT");
        String kept = createVehicle("AKT", type, "Kept bus");
        String added = createVehicle("AKT", type, "Added bus");
        String addedToo = createVehicle("AKT", type, "Added bus too");
        String neverMember = createVehicle("AKT", type, "Never a member");
        String fleetId = createFleet("AKT", "Membership fleet", "members: [ { netexId: \"" + kept + "\" } ] transportTypes: [ { netexId: \"" + type + "\" } ] "
                + "shortName: { value: \"MF\" lang: \"en\" } privateCode: { type: \"depot\" value: \"MF-1\" } keyValues: [ { key: \"garage\" value: \"North\" } ]");

        // When: two vehicles and a vehicle type are added in one call, with a vehicle listed twice and one that already is a member
        graphql(change("addToFleet", fleetId, List.of(added, addedToo, added, kept), List.of(addedType, type)))
                .body("errors", nullValue())
                .body("data.addToFleet", equalTo(fleetId));

        // Then: it is one new version with the three vehicles and the two vehicle types, and the rest of the fleet is as it was
        readFleet(fleetId, "AKT")
                .body("data.fleets.content[0].version", equalTo("2"))
                .body("data.fleets.content[0].members.netexId", org.hamcrest.Matchers.containsInAnyOrder(kept, added, addedToo))
                .body("data.fleets.content[0].transportTypes.netexId", org.hamcrest.Matchers.containsInAnyOrder(type, addedType))
                .body("data.fleets.content[0].name.value", equalTo("Membership fleet"))
                .body("data.fleets.content[0].shortName.value", equalTo("MF"))
                .body("data.fleets.content[0].privateCode.value", equalTo("MF-1"))
                .body("data.fleets.content[0].privateCode.type", equalTo("depot"))
                .body("data.fleets.content[0].keyValues.key", org.hamcrest.Matchers.contains("garage"))
                .body("data.fleets.content[0].keyValues.value", org.hamcrest.Matchers.contains("North"));

        // When: a vehicle and a vehicle type are removed, together with ones that are not in the fleet
        graphql(change("removeFromFleet", fleetId, List.of(added, neverMember), List.of(type, neverInFleet)))
                .body("errors", nullValue())
                .body("data.removeFromFleet", equalTo(fleetId));

        // Then: one more new version without them, and still otherwise unchanged
        readFleet(fleetId, "AKT")
                .body("data.fleets.content[0].version", equalTo("3"))
                .body("data.fleets.content[0].members.netexId", org.hamcrest.Matchers.containsInAnyOrder(kept, addedToo))
                .body("data.fleets.content[0].transportTypes.netexId", org.hamcrest.Matchers.contains(addedType))
                .body("data.fleets.content[0].name.value", equalTo("Membership fleet"))
                .body("data.fleets.content[0].shortName.value", equalTo("MF"))
                .body("data.fleets.content[0].keyValues.value", org.hamcrest.Matchers.contains("North"));
    }

    @Test
    @DisplayName("Vehicles alone or vehicle types alone are enough for a change")
    void eitherKindAloneIsEnough() {
        String type = createVehicleType("AKT");
        String vehicle = createVehicle("AKT", type, "Single kind bus");
        String fleetId = createFleet("AKT", "Single kind fleet", "");

        graphql(change("addToFleet", fleetId, List.of(), List.of(type)))
                .body("errors", nullValue());
        readFleet(fleetId, "AKT")
                .body("data.fleets.content[0].version", equalTo("2"))
                .body("data.fleets.content[0].transportTypes.netexId", org.hamcrest.Matchers.contains(type))
                .body("data.fleets.content[0].members", org.hamcrest.Matchers.empty());

        graphql(change("addToFleet", fleetId, List.of(vehicle), List.of()))
                .body("errors", nullValue());
        readFleet(fleetId, "AKT")
                .body("data.fleets.content[0].version", equalTo("3"))
                .body("data.fleets.content[0].members.netexId", org.hamcrest.Matchers.contains(vehicle))
                .body("data.fleets.content[0].transportTypes.netexId", org.hamcrest.Matchers.contains(type));

        graphql(change("removeFromFleet", fleetId, List.of(), List.of(type)))
                .body("errors", nullValue());
        readFleet(fleetId, "AKT")
                .body("data.fleets.content[0].version", equalTo("4"))
                .body("data.fleets.content[0].members.netexId", org.hamcrest.Matchers.contains(vehicle))
                .body("data.fleets.content[0].transportTypes", org.hamcrest.Matchers.empty());
    }

    @Test
    @DisplayName("A change that cannot be done is rejected as a whole, and the fleet keeps its version")
    void changesThatCannotBeDoneAreRejected() {
        String type = createVehicleType("AKT");
        String vehicle = createVehicle("AKT", type, "Membership bus");
        String other = createVehicle("AKT", type, "Other bus");
        String contractualType = graphql("mutation { createOrUpdateVehicleType(input: { name: { value: \"Contractual\" lang: \"en\" } "
                + "transportMode: BUS vehicleTypeKind: CONTRACTUAL dataOwnerRef: \"AKT\" }) }")
                .body("errors", nullValue()).extract().path("data.createOrUpdateVehicleType");
        String fleetId = createFleet("AKT", "Rejecting fleet", "members: [ { netexId: \"" + vehicle + "\" } ]");

        graphql(change("addToFleet", "AKT:Fleet:does-not-exist", List.of(vehicle), List.of()))
                .body("errors[0].extensions.classification", equalTo("BAD_REQUEST"))
                .body("errors[0].message", containsString("AKT:Fleet:does-not-exist"));
        graphql(change("addToFleet", fleetId, List.of("AKT:Vehicle:does-not-exist"), List.of()))
                .body("errors[0].extensions.classification", equalTo("BAD_REQUEST"))
                .body("errors[0].message", containsString("AKT:Vehicle:does-not-exist"));
        graphql(change("removeFromFleet", fleetId, List.of("AKT:Vehicle:does-not-exist"), List.of()))
                .body("errors[0].extensions.classification", equalTo("BAD_REQUEST"));
        graphql(change("addToFleet", fleetId, List.of(), List.of("AKT:VehicleType:does-not-exist")))
                .body("errors[0].extensions.classification", equalTo("BAD_REQUEST"))
                .body("errors[0].message", containsString("AKT:VehicleType:does-not-exist"));
        graphql(change("addToFleet", fleetId, List.of(), List.of()))
                .body("errors[0].extensions.classification", equalTo("BAD_REQUEST"))
                .body("errors[0].message", containsString("At least one vehicle or vehicle type"));

        // A fleet can only have physical vehicle types
        graphql(change("addToFleet", fleetId, List.of(), List.of(contractualType)))
                .body("errors[0].extensions.classification", equalTo("BAD_REQUEST"))
                .body("errors[0].message", containsString("contractual"));

        // One thing that is refused refuses the whole call: the vehicle that was fine is not added either
        graphql(change("addToFleet", fleetId, List.of(other), List.of(contractualType)))
                .body("errors[0].extensions.classification", equalTo("BAD_REQUEST"));

        readFleet(fleetId, "AKT")
                .body("data.fleets.content[0].version", equalTo("1"))
                .body("data.fleets.content[0].members.netexId", org.hamcrest.Matchers.contains(vehicle))
                .body("data.fleets.content[0].transportTypes", org.hamcrest.Matchers.empty());
    }

    /** An addToFleet or removeFromFleet mutation for the fleet, with the vehicles and the vehicle types that are not empty. */
    private static String change(String mutation, String fleetId, List<String> vehicleIds, List<String> vehicleTypeIds) {
        return "mutation { " + mutation + "(input: { fleetNetexId: \"" + fleetId + "\" "
                + references("vehicles", vehicleIds) + references("transportTypes", vehicleTypeIds) + "}) }";
    }

    private static String references(String field, List<String> netexIds) {
        if (netexIds.isEmpty()) {
            return "";
        }
        StringBuilder references = new StringBuilder(field + ": [ ");
        for (String netexId : netexIds) {
            references.append("{ netexId: \"").append(netexId).append("\" } ");
        }
        return references.append("] ").toString();
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

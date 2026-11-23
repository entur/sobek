package org.rutebanken.sobek.rest.graphql;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.rutebanken.sobek.auth.AuthorizationService;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

/**
 * Sharing a fleet: shareFleet and unshareFleet, and what the organisations it is shared with see when they query for
 * their own data owner. The fleets are owned by AKT and shared with organisation B or with everyone, and read as B or as a
 * third organisation (C), which the fleets are never shared with by name. The user is authorized for all three (see setUp).
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class FleetSharingGraphQLTest {

    private static final String OWNER = "AKT";
    /** Organisations of the organisation registry of the tests. */
    private static final String SHARED_WITH = "NOG:Authority:23e9gDqTGNg";
    private static final String OTHER = "NOG:Operator:earJinAJlwf";

    private static final DateTimeFormatter DEACTIVATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withZone(ZoneOffset.UTC);

    @LocalServerPort
    int port;

    /** The tests run as a user who is authorized for AKT only. Here the user is authorized for the three organisations of the test. */
    @MockitoSpyBean
    AuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        List<String> organisations = List.of(OWNER, SHARED_WITH, OTHER);
        doReturn(organisations).when(authorizationService).getOrganisationRefsUserIsAuthorizedFor();
        doNothing().when(authorizationService).verifyCanViewDataOwner(argThat(organisations::contains));
    }

    @Test
    @DisplayName("A fleet shared with an organisation is seen by it, with its vehicles and vehicle types, and by no one else")
    void sharedFleetIsSeenByTheOrganisation() {
        Fixture fixture = fixture("Shared");

        // Before it is shared, only the owner sees it
        assertSeen(SHARED_WITH, fixture, false);

        graphql("mutation { shareFleet(input: { fleetNetexId: \"" + fixture.fleet + "\" organisationRefs: [\"" + SHARED_WITH + "\"] }) }")
                .body("errors", nullValue())
                .body("data.shareFleet", equalTo(fixture.fleet));

        assertSeen(SHARED_WITH, fixture, true);
        assertSeen(OWNER, fixture, true);
        assertSeen(OTHER, fixture, false);

        // The user of this test is authorized for the owner too, so the vehicles of the type are all of the owner's. What a user
        // of B alone sees of them is tested in FleetVisibilityGraphQLTest.
        graphql("{ vehicleTypes(filter: { netexIds: [\"" + fixture.type + "\"] vehicleTypeKind: PHYSICAL dataOwnerRef: \"" + OWNER + "\" }) "
                + "{ content { vehicles { netexId } } } }")
                .body("data.vehicleTypes.content[0].vehicles.netexId", containsInAnyOrder(fixture.member, fixture.outsider));
    }

    @Test
    @DisplayName("A fleet shared with everyone is seen by any organisation")
    void fleetSharedWithEveryoneIsSeenByAll() {
        Fixture fixture = fixture("Everyone");

        graphql("mutation { shareFleet(input: { fleetNetexId: \"" + fixture.fleet + "\" everyone: true }) }")
                .body("errors", nullValue());

        assertSeen(SHARED_WITH, fixture, true);
        assertSeen(OTHER, fixture, true);
        readSharing(fixture.fleet, OWNER)
                .body("data.fleets.content[0].sharedWithAll", equalTo(true))
                .body("data.fleets.content[0].sharedWith", empty());
    }

    @Test
    @DisplayName("Unsharing takes the fleet away from the organisations, one by one, and from everyone")
    void unsharing() {
        Fixture fixture = fixture("Unshare");
        graphql("mutation { shareFleet(input: { fleetNetexId: \"" + fixture.fleet + "\" everyone: true organisationRefs: [\"" + SHARED_WITH + "\"] }) }")
                .body("errors", nullValue());
        readSharing(fixture.fleet, OWNER)
                .body("data.fleets.content[0].version", equalTo("2"))
                .body("data.fleets.content[0].sharedWithAll", equalTo(true))
                .body("data.fleets.content[0].sharedWith", containsInAnyOrder(SHARED_WITH));

        // Everyone is unshared, and the organisation by name still sees it
        graphql("mutation { unshareFleet(input: { fleetNetexId: \"" + fixture.fleet + "\" everyone: true }) }")
                .body("errors", nullValue());
        assertSeen(OTHER, fixture, false);
        assertSeen(SHARED_WITH, fixture, true);

        // The organisation is unshared, together with one the fleet was never shared with
        graphql("mutation { unshareFleet(input: { fleetNetexId: \"" + fixture.fleet + "\" organisationRefs: [\"" + SHARED_WITH + "\", \"" + OTHER + "\"] }) }")
                .body("errors", nullValue());
        assertSeen(SHARED_WITH, fixture, false);
        readSharing(fixture.fleet, OWNER)
                .body("data.fleets.content[0].version", equalTo("4"))
                .body("data.fleets.content[0].sharedWithAll", equalTo(false))
                .body("data.fleets.content[0].sharedWith", empty());
    }

    @Test
    @DisplayName("The sharing is kept when the fleet is updated, and when vehicles are added to it")
    void sharingFollowsTheNewVersions() {
        Fixture fixture = fixture("Follows");
        graphql("mutation { shareFleet(input: { fleetNetexId: \"" + fixture.fleet + "\" organisationRefs: [\"" + SHARED_WITH + "\"] }) }")
                .body("errors", nullValue());

        graphql("mutation { createOrUpdateFleet(input: { netexId: \"" + fixture.fleet + "\" name: { value: \"Renamed\" lang: \"en\" } "
                + "members: [ { netexId: \"" + fixture.member + "\" } ] transportTypes: [ { netexId: \"" + fixture.type + "\" } ] dataOwnerRef: \"" + OWNER + "\" }) }")
                .body("errors", nullValue());
        graphql("mutation { addToFleet(input: { fleetNetexId: \"" + fixture.fleet + "\" vehicles: [ { netexId: \"" + fixture.outsider + "\" } ] }) }")
                .body("errors", nullValue());

        readSharing(fixture.fleet, OWNER)
                .body("data.fleets.content[0].version", equalTo("4"))
                .body("data.fleets.content[0].sharedWith", containsInAnyOrder(SHARED_WITH));
        // The vehicle that was added is shared with the fleet
        graphql("{ vehicles(filter: { netexIds: [\"" + fixture.outsider + "\"] dataOwnerRef: \"" + SHARED_WITH + "\" }) { totalElements } }")
                .body("data.vehicles.totalElements", equalTo(1));
    }

    @Test
    @DisplayName("A shared fleet that is deactivated is no longer seen by the organisation")
    void deactivatedFleetIsNotSeen() {
        Fixture fixture = fixture("Deactivated");
        graphql("mutation { shareFleet(input: { fleetNetexId: \"" + fixture.fleet + "\" organisationRefs: [\"" + SHARED_WITH + "\"] }) }")
                .body("errors", nullValue());
        assertSeen(SHARED_WITH, fixture, true);

        // Deactivated now: the version that makes it so is the latest, and it is not valid any more
        graphql("mutation { deactivateFleet(input: { netexId: \"" + fixture.fleet + "\" version: 2 deactivateAt: \"" + DEACTIVATE_FORMAT.format(Instant.now()) + "\" }) { netexId } }")
                .body("errors", nullValue());
        assertSeen(SHARED_WITH, fixture, false);
    }

    @Test
    @DisplayName("Sharing needs an organisation or everyone, an organisation that exists, and not the owner")
    void sharingThatCannotBeDoneIsRejected() {
        Fixture fixture = fixture("Rejected");

        graphql("mutation { shareFleet(input: { fleetNetexId: \"" + fixture.fleet + "\" }) }")
                .body("errors[0].extensions.classification", equalTo("BAD_REQUEST"))
                .body("errors[0].message", containsString("at least one organisation"));
        graphql("mutation { unshareFleet(input: { fleetNetexId: \"" + fixture.fleet + "\" organisationRefs: [] }) }")
                .body("errors[0].extensions.classification", equalTo("BAD_REQUEST"));
        graphql("mutation { shareFleet(input: { fleetNetexId: \"" + fixture.fleet + "\" organisationRefs: [\"" + OWNER + "\"] }) }")
                .body("errors[0].extensions.classification", equalTo("BAD_REQUEST"))
                .body("errors[0].message", containsString("owner"));
        graphql("mutation { shareFleet(input: { fleetNetexId: \"" + fixture.fleet + "\" organisationRefs: [\"NO-SUCH-ORGANISATION\"] }) }")
                .body("errors[0].extensions.classification", equalTo("BAD_REQUEST"));
        graphql("mutation { shareFleet(input: { fleetNetexId: \"AKT:Fleet:does-not-exist\" everyone: true }) }")
                .body("errors[0].extensions.classification", equalTo("BAD_REQUEST"))
                .body("errors[0].message", containsString("AKT:Fleet:does-not-exist"));

        // Nothing was changed
        readSharing(fixture.fleet, OWNER)
                .body("data.fleets.content[0].version", equalTo("1"))
                .body("data.fleets.content[0].sharedWithAll", equalTo(false));
    }

    /** What the organisation sees of the fixture: the fleet with its members and transport types, the vehicle, and the vehicle type. */
    private void assertSeen(String organisation, Fixture fixture, boolean expected) {
        String filter = "dataOwnerRef: \"" + organisation + "\" }";
        ValidatableResponse fleets = graphql("{ fleets(filter: { netexIds: [\"" + fixture.fleet + "\"] " + filter + ") "
                + "{ content { netexId members { netexId } transportTypes { netexId } } } }")
                .body("errors", nullValue());
        graphql("{ vehicles(filter: { netexIds: [\"" + fixture.member + "\", \"" + fixture.outsider + "\"] " + filter + ") { content { netexId } } }")
                .body("errors", nullValue())
                .body("data.vehicles.content.netexId", expected ? hasItem(fixture.member) : not(hasItem(fixture.member)))
                // The vehicle that is not in the fleet is seen by the owner only
                .body("data.vehicles.content.netexId", organisation.equals(OWNER) ? hasItem(fixture.outsider) : not(hasItem(fixture.outsider)));
        graphql("{ vehicleTypes(filter: { netexIds: [\"" + fixture.type + "\"] vehicleTypeKind: PHYSICAL " + filter + ") { content { netexId } } }")
                .body("errors", nullValue())
                .body("data.vehicleTypes.content.netexId", expected ? hasItem(fixture.type) : not(hasItem(fixture.type)));
        if (expected) {
            fleets.body("data.fleets.content[0].netexId", equalTo(fixture.fleet))
                    .body("data.fleets.content[0].members.netexId", containsInAnyOrder(fixture.member))
                    .body("data.fleets.content[0].transportTypes.netexId", containsInAnyOrder(fixture.type));
        } else {
            fleets.body("data.fleets.content", empty());
        }
    }

    /** A fleet with a vehicle and a vehicle type, and a vehicle of the same type that is not in the fleet. All owned by AKT. */
    private record Fixture(String fleet, String type, String member, String outsider) {
    }

    private Fixture fixture(String name) {
        String type = graphql("mutation { createOrUpdateVehicleType(input: { name: { value: \"Sharing " + name + " type\" lang: \"en\" } transportMode: BUS "
                + "vehicleTypeKind: PHYSICAL dataOwnerRef: \"" + OWNER + "\" }) }")
                .body("errors", nullValue()).extract().path("data.createOrUpdateVehicleType");
        String member = createVehicle(type, "Sharing " + name + " member");
        String outsider = createVehicle(type, "Sharing " + name + " outsider");
        String fleet = graphql("mutation { createOrUpdateFleet(input: { name: { value: \"Sharing " + name + " fleet\" lang: \"en\" } "
                + "members: [ { netexId: \"" + member + "\" } ] transportTypes: [ { netexId: \"" + type + "\" } ] dataOwnerRef: \"" + OWNER + "\" }) }")
                .body("errors", nullValue()).extract().path("data.createOrUpdateFleet");
        return new Fixture(fleet, type, member, outsider);
    }

    private String createVehicle(String vehicleType, String name) {
        return graphql("mutation { createOrUpdateVehicle(input: { name: { value: \"" + name + "\" lang: \"en\" } "
                + "transportType: { netexId: \"" + vehicleType + "\" } dataOwnerRef: \"" + OWNER + "\" }) }")
                .body("errors", nullValue())
                .extract().path("data.createOrUpdateVehicle");
    }

    private ValidatableResponse readSharing(String fleetId, String dataOwner) {
        return graphql("{ fleets(filter: { netexIds: [\"" + fleetId + "\"] dataOwnerRef: \"" + dataOwner + "\" }) "
                + "{ content { netexId version sharedWithAll sharedWith } } }");
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

package org.rutebanken.sobek.rest.graphql;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rutebanken.sobek.auth.AuthorizationService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

/**
 * What users of other organisations must NOT see. Data is created as the owner (AKT), and read as a user who is authorized
 * for exactly one other organisation (B or C), so that nothing is let through by the user also being authorized for the owner.
 * A fleet that is not shared with the organisation of the user is invisible to it: the fleet itself, its vehicles and its
 * vehicle types, however they are asked for (by id, by name, by count, or through the fleets of a vehicle or a vehicle type).
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class FleetVisibilityGraphQLTest {

    private static final String OWNER = "AKT";
    /** Organisations of the organisation registry of the tests. */
    private static final String B = "NOG:Authority:23e9gDqTGNg";
    private static final String C = "NOG:Operator:earJinAJlwf";

    @LocalServerPort
    int port;

    @MockitoSpyBean
    AuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("A fleet that is not shared is invisible to another organisation, with its vehicles and vehicle types")
    void unsharedFleetIsInvisible() {
        Fixture fixture = fixture("Private");

        actAs(B);
        assertInvisible(fixture);
        actAs(C);
        assertInvisible(fixture);

        actAs(OWNER);
        assertVisible(fixture);
    }

    @Test
    @DisplayName("A fleet that is shared with one organisation is invisible to the others")
    void fleetSharedWithOthersIsInvisible() {
        Fixture fixture = fixture("SharedWithC");
        share(fixture.fleet, C);

        actAs(B);
        assertInvisible(fixture);
        actAs(C);
        assertVisible(fixture);
    }

    @Test
    @DisplayName("A fleet that is unshared again is invisible, and so is one that is deactivated")
    void unsharedAgainAndDeactivatedFleetsAreInvisible() {
        Fixture unshared = fixture("Unshared again");
        share(unshared.fleet, B);
        actAs(B);
        assertVisible(unshared);
        actAs(OWNER);
        graphql("mutation { unshareFleet(input: { fleetNetexId: \"" + unshared.fleet + "\" organisationRefs: [\"" + B + "\"] }) }")
                .body("errors", nullValue());
        actAs(B);
        assertInvisible(unshared);

        Fixture deactivated = fixture("Deactivated");
        share(deactivated.fleet, B);
        actAs(OWNER);
        graphql("mutation { deactivateFleet(input: { netexId: \"" + deactivated.fleet + "\" version: 2 deactivateAt: \""
                + java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withZone(java.time.ZoneOffset.UTC).format(java.time.Instant.now())
                + "\" }) { netexId } }")
                .body("errors", nullValue());
        actAs(B);
        assertInvisible(deactivated);
    }

    @Test
    @DisplayName("Of two fleets that share a vehicle and a vehicle type, only the shared one is seen, also through the fleets of the vehicle and the type")
    void theUnsharedFleetOfASharedVehicleIsNotSeen() {
        Fixture shared = fixture("Both shared");
        // A second fleet of the owner, not shared, with the same vehicle and the same vehicle type
        String privateFleet = createFleet("Both private", shared.member, shared.type);
        share(shared.fleet, B);

        actAs(B);
        // The fleets query
        graphql("{ fleets(filter: { name: \"Both\" dataOwnerRef: \"" + B + "\" }) { totalElements content { netexId } } }")
                .body("errors", nullValue())
                .body("data.fleets.content.netexId", containsInAnyOrder(shared.fleet))
                .body("data.fleets.totalElements", equalTo(1));
        // The fleets of the vehicle and of the vehicle type
        graphql("{ vehicles(filter: { netexIds: [\"" + shared.member + "\"] dataOwnerRef: \"" + B + "\" }) { content { fleets { netexId } } } }")
                .body("errors", nullValue())
                .body("data.vehicleTypes.content[0].fleets.netexId", not(hasItem(privateFleet)))
                .body("data.vehicles.content[0].fleets.netexId", containsInAnyOrder(shared.fleet));
        graphql("{ vehicleTypes(filter: { netexIds: [\"" + shared.type + "\"] vehicleTypeKind: PHYSICAL dataOwnerRef: \"" + B + "\" }) { content { fleets { netexId } } } }")
                .body("errors", nullValue())
                .body("data.vehicleTypes.content[0].fleets.netexId", not(hasItem(privateFleet)))
                .body("data.vehicleTypes.content[0].fleets.netexId", containsInAnyOrder(shared.fleet));

        // The owner sees both
        actAs(OWNER);
        graphql("{ vehicles(filter: { netexIds: [\"" + shared.member + "\"] dataOwnerRef: \"" + OWNER + "\" }) { content { fleets { netexId } } } }")
                .body("data.vehicles.content[0].fleets.netexId", containsInAnyOrder(shared.fleet, privateFleet));
    }

    @Test
    @DisplayName("A user of another organisation cannot ask for the data of the owner, or for the data of everyone")
    void otherOrganisationsCannotAskForTheOwnersData() {
        Fixture fixture = fixture("Forbidden");
        share(fixture.fleet, B);

        actAs(B);
        for (String query : List.of(
                "fleets(filter: { dataOwnerRef: \"" + OWNER + "\" }) { totalElements }",
                "vehicles(filter: { dataOwnerRef: \"" + OWNER + "\" }) { totalElements }",
                "vehicleTypes(filter: { vehicleTypeKind: PHYSICAL dataOwnerRef: \"" + OWNER + "\" }) { totalElements }",
                "fleets(filter: { netexIds: [\"" + fixture.fleet + "\"] dataOwnerRef: \"" + OWNER + "\" }) { totalElements }")) {
            graphql("{ " + query + " }").body("errors[0].extensions.classification", equalTo("FORBIDDEN"));
        }
        // Without a data owner
        graphql("{ fleets(filter: { name: \"Forbidden\" }) { totalElements } }").body("errors", org.hamcrest.Matchers.notNullValue());
        graphql("{ fleets { totalElements } }").body("errors", org.hamcrest.Matchers.notNullValue());
    }

    @Test
    @DisplayName("Another organisation cannot change the sharing of a fleet, or the fleet")
    void otherOrganisationsCannotChangeTheSharing() {
        Fixture fixture = fixture("Not yours");

        // The user can edit data of B only (a fleet of AKT is refused when it is saved)
        doAnswer(invocation -> {
            ((java.util.Collection<?>) invocation.getArgument(0)).forEach(entity -> {
                if (entity instanceof org.rutebanken.sobek.model.authorization.OwnedEntity owned && !B.equals(owned.getDataOwnerRef())) {
                    throw new AccessDeniedException("Not authorized to edit data owned by '" + owned.getDataOwnerRef() + "'");
                }
            });
            return null;
        }).when(authorizationService).verifyCanEditEntities(any());

        graphql("mutation { shareFleet(input: { fleetNetexId: \"" + fixture.fleet + "\" organisationRefs: [\"" + C + "\"] }) }")
                .body("errors[0].extensions.classification", equalTo("FORBIDDEN"));
        graphql("mutation { shareFleet(input: { fleetNetexId: \"" + fixture.fleet + "\" everyone: true }) }")
                .body("errors[0].extensions.classification", equalTo("FORBIDDEN"));
        graphql("mutation { unshareFleet(input: { fleetNetexId: \"" + fixture.fleet + "\" everyone: true }) }")
                .body("errors[0].extensions.classification", equalTo("FORBIDDEN"));

        // Nothing changed: the fleet is still a version 1 that is shared with no one
        org.mockito.Mockito.reset(authorizationService);
        actAs(OWNER);
        graphql("{ fleets(filter: { netexIds: [\"" + fixture.fleet + "\"] dataOwnerRef: \"" + OWNER + "\" }) { content { version sharedWithAll sharedWith } } }")
                .body("data.fleets.content[0].version", equalTo("1"))
                .body("data.fleets.content[0].sharedWithAll", equalTo(false))
                .body("data.fleets.content[0].sharedWith", empty());
    }

    /** The user is authorized to see the data of the organisation only. Editing stays as the tests have it (AKT). */
    private void actAs(String organisation) {
        doReturn(List.of(organisation)).when(authorizationService).getOrganisationRefsUserIsAuthorizedFor();
        doAnswer(invocation -> {
            if (!organisation.equals(invocation.getArgument(0))) {
                throw new AccessDeniedException("Not authorized to access data owned by '" + invocation.getArgument(0) + "'");
            }
            return null;
        }).when(authorizationService).verifyCanViewDataOwner(any());
    }

    /** Nothing of the fixture is seen by the current user, asked for with the data owner of the user. */
    private void assertInvisible(Fixture fixture) {
        String organisation = currentOrganisation();
        String owner = "dataOwnerRef: \"" + organisation + "\" }";
        graphql("{ fleets(filter: { netexIds: [\"" + fixture.fleet + "\"] " + owner + ") { totalElements content { netexId } } }")
                .body("errors", nullValue())
                .body("data.fleets.totalElements", equalTo(0))
                .body("data.fleets.content", empty());
        graphql("{ fleets(filter: { name: \"" + fixture.name + "\" " + owner + ") { totalElements content { netexId } } }")
                .body("errors", nullValue())
                .body("data.fleets.totalElements", equalTo(0));
        graphql("{ vehicles(filter: { netexIds: [\"" + fixture.member + "\", \"" + fixture.outsider + "\"] " + owner + ") { totalElements content { netexId } } }")
                .body("errors", nullValue())
                .body("data.vehicles.totalElements", equalTo(0))
                .body("data.vehicles.content", empty());
        graphql("{ vehicles(filter: { name: \"" + fixture.name + "\" " + owner + ") { totalElements } }")
                .body("data.vehicles.totalElements", equalTo(0));
        graphql("{ vehicleTypes(filter: { netexIds: [\"" + fixture.type + "\"] vehicleTypeKind: PHYSICAL " + owner + ") { totalElements content { netexId } } }")
                .body("errors", nullValue())
                .body("data.vehicleTypes.totalElements", equalTo(0))
                .body("data.vehicleTypes.content", empty());
        graphql("{ vehicleTypes(filter: { name: \"" + fixture.name + "\" vehicleTypeKind: PHYSICAL " + owner + ") { totalElements } }")
                .body("data.vehicleTypes.totalElements", equalTo(0));
    }

    /** The fleet, its vehicle and its vehicle type are seen by the current user, and the vehicle that is not in the fleet is seen only by the owner. */
    private void assertVisible(Fixture fixture) {
        String organisation = currentOrganisation();
        String owner = "dataOwnerRef: \"" + organisation + "\" }";
        graphql("{ fleets(filter: { netexIds: [\"" + fixture.fleet + "\"] " + owner + ") { content { netexId } } }")
                .body("errors", nullValue())
                .body("data.fleets.content.netexId", containsInAnyOrder(fixture.fleet));
        graphql("{ vehicles(filter: { netexIds: [\"" + fixture.member + "\", \"" + fixture.outsider + "\"] " + owner + ") { content { netexId } } }")
                .body("errors", nullValue())
                .body("data.vehicles.content.netexId", organisation.equals(OWNER)
                        ? containsInAnyOrder(fixture.member, fixture.outsider) : containsInAnyOrder(fixture.member));
        graphql("{ vehicleTypes(filter: { netexIds: [\"" + fixture.type + "\"] vehicleTypeKind: PHYSICAL " + owner + ") { content { netexId vehicles { netexId } } } }")
                .body("errors", nullValue())
                .body("data.vehicleTypes.content.netexId", containsInAnyOrder(fixture.type))
                .body("data.vehicleTypes.content[0].vehicles.netexId", organisation.equals(OWNER)
                        ? containsInAnyOrder(fixture.member, fixture.outsider) : containsInAnyOrder(fixture.member));
    }

    private String currentOrganisation() {
        return authorizationService.getOrganisationRefsUserIsAuthorizedFor().get(0);
    }

    /** A fleet with a vehicle and a vehicle type, and a vehicle of the same type that is not in the fleet. All owned by AKT. */
    private record Fixture(String name, String fleet, String type, String member, String outsider) {
    }

    private Fixture fixture(String name) {
        actAs(OWNER);
        String unique = "Vis " + name + " " + System.nanoTime();
        String type = graphql("mutation { createOrUpdateVehicleType(input: { name: { value: \"" + unique + " type\" lang: \"en\" } transportMode: BUS "
                + "vehicleTypeKind: PHYSICAL dataOwnerRef: \"" + OWNER + "\" }) }")
                .body("errors", nullValue()).extract().path("data.createOrUpdateVehicleType");
        String member = createVehicle(type, unique + " member");
        String outsider = createVehicle(type, unique + " outsider");
        String fleet = createFleet(unique + " fleet", member, type);
        return new Fixture(unique, fleet, type, member, outsider);
    }

    private String createVehicle(String vehicleType, String name) {
        return graphql("mutation { createOrUpdateVehicle(input: { name: { value: \"" + name + "\" lang: \"en\" } "
                + "transportType: { netexId: \"" + vehicleType + "\" } dataOwnerRef: \"" + OWNER + "\" }) }")
                .body("errors", nullValue()).extract().path("data.createOrUpdateVehicle");
    }

    private String createFleet(String name, String member, String type) {
        return graphql("mutation { createOrUpdateFleet(input: { name: { value: \"" + name + "\" lang: \"en\" } "
                + "members: [ { netexId: \"" + member + "\" } ] transportTypes: [ { netexId: \"" + type + "\" } ] dataOwnerRef: \"" + OWNER + "\" }) }")
                .body("errors", nullValue()).extract().path("data.createOrUpdateFleet");
    }

    private void share(String fleet, String organisation) {
        String viewer = authorizationService.getOrganisationRefsUserIsAuthorizedFor() == null ? OWNER : currentOrganisation();
        actAs(OWNER);
        graphql("mutation { shareFleet(input: { fleetNetexId: \"" + fleet + "\" organisationRefs: [\"" + organisation + "\"] }) }")
                .body("errors", nullValue());
        actAs(viewer);
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

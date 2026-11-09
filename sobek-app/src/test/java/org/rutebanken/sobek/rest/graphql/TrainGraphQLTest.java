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

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

/**
 * Trains and compound trains through GraphQL: the same createOrUpdateVehicleType and vehicleTypes as for any vehicle
 * type, with a subtype that says what is created and the train fields that go with it.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class TrainGraphQLTest {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("A train is created with its size and components, and read back with the components in order")
    void trainWithComponentsIsCreatedAndReadBack() {
        // Given: two train elements
        String front = createTrainElement("ENGINE");
        String rear = createTrainElement("SLEEPER_CARRIAGE");

        // When: a train is created with a component that has an id of its own and one that has none
        String trainId = createTrain("trainComponents: [ "
                + "{ netexId: \"AKT:TrainComponent:gql-first\" label: { value: \"first car\" lang: \"en\" } trainElement: { netexId: \"" + front + "\" } "
                + "  operationalOrientation: FORWARDS forwardCouplingThroughAccess: DOOR }, "
                + "{ label: { value: \"second car\" lang: \"en\" } trainElement: { netexId: \"" + rear + "\" } operationalOrientation: BACKWARDS } ]");

        // Then: it is a train with the size and the components as given, in the order given
        String components = "trainComponents { netexId label { value } trainElement { netexId subtype trainElementType } operationalOrientation forwardCouplingThroughAccess }";
        ValidatableResponse read = graphql("{ vehicleTypes(filter: { netexIds: [\"" + trainId + "\"] dataOwnerRef: \"AKT\" vehicleTypeKind: PHYSICAL }) "
                + "{ content { subtype numberOfCars trainSizeType compoundTrainComponents { netexId } " + components + " } } }")
                .body("errors", nullValue())
                .body("data.vehicleTypes.content[0].subtype", equalTo("TRAIN"))
                .body("data.vehicleTypes.content[0].numberOfCars", equalTo(2))
                .body("data.vehicleTypes.content[0].trainSizeType", equalTo("SHORT"))
                .body("data.vehicleTypes.content[0].compoundTrainComponents", nullValue());
        read.body("data.vehicleTypes.content[0].trainComponents.label.value", org.hamcrest.Matchers.contains("first car", "second car"))
                .body("data.vehicleTypes.content[0].trainComponents.trainElement.netexId", org.hamcrest.Matchers.contains(front, rear))
                .body("data.vehicleTypes.content[0].trainComponents.trainElement.subtype", org.hamcrest.Matchers.contains("TRAIN_ELEMENT", "TRAIN_ELEMENT"))
                .body("data.vehicleTypes.content[0].trainComponents.trainElement.trainElementType", org.hamcrest.Matchers.contains("ENGINE", "SLEEPER_CARRIAGE"))
                .body("data.vehicleTypes.content[0].trainComponents[0].forwardCouplingThroughAccess", equalTo("DOOR"))
                .body("data.vehicleTypes.content[0].trainComponents[1].operationalOrientation", equalTo("BACKWARDS"));

        // And: the id the client gave a component is kept, and the other component got one from Sobek
        assertThat(read.extract().<String>path("data.vehicleTypes.content[0].trainComponents[0].netexId")).isEqualTo("AKT:TrainComponent:gql-first");
        assertThat(read.extract().<String>path("data.vehicleTypes.content[0].trainComponents[1].netexId")).startsWith("NMR:TrainComponent:");
    }

    @Test
    @DisplayName("A train element is created with its element type and size, and is found by its subtype")
    void trainElementIsCreatedAndReadBack() {
        String elementId = createVehicleType("subtype: TRAIN_ELEMENT vehicleTypeKind: PHYSICAL trainElementType: BUFFET_CAR length: 26.4 lowFloor: true");

        graphql("{ vehicleTypes(filter: { netexIds: [\"" + elementId + "\"] dataOwnerRef: \"AKT\" vehicleTypeKind: PHYSICAL subtype: TRAIN_ELEMENT }) "
                + "{ content { subtype trainElementType length lowFloor numberOfCars trainComponents { netexId } } totalElements } }")
                .body("errors", nullValue())
                .body("data.vehicleTypes.totalElements", equalTo(1))
                .body("data.vehicleTypes.content[0].subtype", equalTo("TRAIN_ELEMENT"))
                .body("data.vehicleTypes.content[0].trainElementType", equalTo("BUFFET_CAR"))
                .body("data.vehicleTypes.content[0].length", equalTo(26.4f))
                .body("data.vehicleTypes.content[0].lowFloor", equalTo(true))
                .body("data.vehicleTypes.content[0].numberOfCars", nullValue())
                .body("data.vehicleTypes.content[0].trainComponents", nullValue());
        assertThat(countOf(elementId, "TRAIN")).as("a train element is not a train").isZero();
    }

    @Test
    @DisplayName("A contractual train element is in the contractual codespace")
    void contractualTrainElementIsInTheContractualCodespace() {
        String elementId = createVehicleType("subtype: TRAIN_ELEMENT vehicleTypeKind: CONTRACTUAL trainElementType: CARRIAGE");

        assertThat(elementId).startsWith("CVT:TrainElement:");
    }

    @Test
    @DisplayName("A train can only be made of train elements that exist")
    void trainOfNonElementsIsRejected() {
        String otherTrain = createTrain("");

        graphql(createMutation("subtype: TRAIN vehicleTypeKind: PHYSICAL trainComponents: [ { trainElement: { netexId: \"" + otherTrain + "\" } } ]"))
                .body("errors[0].extensions.classification", equalTo("BAD_REQUEST"))
                .body("errors[0].message", containsString("Only train elements can be put in a train"));

        graphql(createMutation("subtype: TRAIN vehicleTypeKind: PHYSICAL trainComponents: [ { trainElement: { netexId: \"AKT:TrainElement:does-not-exist\" } } ]"))
                .body("errors[0].extensions.classification", equalTo("BAD_REQUEST"))
                .body("errors[0].message", containsString("not found"));
    }

    @Test
    @DisplayName("A train element that is part of a train cannot be deactivated")
    void trainElementInUseCannotBeDeactivated() {
        String elementId = createTrainElement("CARRIAGE");
        createTrain("trainComponents: [ { trainElement: { netexId: \"" + elementId + "\" } } ]");
        String version = graphql("{ vehicleTypes(filter: { netexIds: [\"" + elementId + "\"] dataOwnerRef: \"AKT\" vehicleTypeKind: PHYSICAL }) { content { version } } }")
                .extract().path("data.vehicleTypes.content[0].version");

        graphql("mutation { deactivateVehicleType(input: { netexId: \"" + elementId + "\" version: " + version + " deactivateAt: \"2099-01-01T00:00:00.000+0000\" }) { netexId } }")
                .body("errors[0].extensions.classification", equalTo("BAD_REQUEST"))
                .body("errors[0].message", containsString("still part of a train"));
    }

    @Test
    @DisplayName("A compound train is created from trains, and the trains are read back in order")
    void compoundTrainIsCreatedFromTrains() {
        String front = createTrain("");
        String rear = createTrain("");

        // When: the rear train is listed before the front train
        String compoundId = createVehicleType("subtype: COMPOUND_TRAIN vehicleTypeKind: PHYSICAL compoundTrainComponents: [ "
                + "{ train: { netexId: \"" + rear + "\" } label: { value: \"rear\" lang: \"en\" } operationalOrientation: BACKWARDS }, "
                + "{ train: { netexId: \"" + front + "\" } label: { value: \"front\" lang: \"en\" } operationalOrientation: FORWARDS } ]");

        // Then: it is a compound train that holds the trains as trains, in that order
        graphql("{ vehicleTypes(filter: { netexIds: [\"" + compoundId + "\"] dataOwnerRef: \"AKT\" vehicleTypeKind: PHYSICAL }) "
                + "{ content { subtype trainComponents { netexId } compoundTrainComponents { netexId label { value } operationalOrientation train { netexId subtype } } } } }")
                .body("errors", nullValue())
                .body("data.vehicleTypes.content[0].subtype", equalTo("COMPOUND_TRAIN"))
                .body("data.vehicleTypes.content[0].trainComponents", nullValue())
                .body("data.vehicleTypes.content[0].compoundTrainComponents.train.netexId", org.hamcrest.Matchers.contains(rear, front))
                .body("data.vehicleTypes.content[0].compoundTrainComponents.train.subtype", org.hamcrest.Matchers.contains("TRAIN", "TRAIN"))
                .body("data.vehicleTypes.content[0].compoundTrainComponents.label.value", org.hamcrest.Matchers.contains("rear", "front"))
                .body("data.vehicleTypes.content[0].compoundTrainComponents.netexId", org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.startsWith("NMR:TrainInCompoundTrain:")));
    }

    @Test
    @DisplayName("The subtype filter returns only vehicle types of exactly that subtype")
    void subtypeFilterSelectsOnlyThatSubtype() {
        String trainId = createTrain("");

        assertThat(countOf(trainId, "TRAIN")).isEqualTo(1);
        assertThat(countOf(trainId, "COMPOUND_TRAIN")).isZero();
        assertThat(countOf(trainId, "VEHICLE_TYPE")).as("a train is not a plain vehicle type").isZero();
        assertThat(countOf(trainId, null)).as("no subtype filter returns all subtypes").isEqualTo(1);
    }

    @Test
    @DisplayName("A compound train cannot hold something that is not a train, or a train that does not exist")
    void compoundTrainOfNonTrainsIsRejected() {
        String plain = createVehicleType("vehicleTypeKind: PHYSICAL");

        graphql(createMutation("subtype: COMPOUND_TRAIN vehicleTypeKind: PHYSICAL compoundTrainComponents: [ { train: { netexId: \"" + plain + "\" } } ]"))
                .body("errors[0].extensions.classification", equalTo("BAD_REQUEST"))
                .body("errors[0].message", containsString("Only trains can be put in a compound train"));

        graphql(createMutation("subtype: COMPOUND_TRAIN vehicleTypeKind: PHYSICAL compoundTrainComponents: [ { train: { netexId: \"AKT:Train:does-not-exist\" } } ]"))
                .body("errors[0].extensions.classification", equalTo("BAD_REQUEST"))
                .body("errors[0].message", containsString("not found"));
    }

    @Test
    @DisplayName("Train fields are rejected on another subtype, and a new version cannot change the subtype")
    void subtypeRulesAreEnforced() {
        graphql(createMutation("vehicleTypeKind: PHYSICAL numberOfCars: 3"))
                .body("errors[0].extensions.classification", equalTo("BAD_REQUEST"))
                .body("errors[0].message", org.hamcrest.Matchers.allOf(containsString("numberOfCars"), containsString("VEHICLE_TYPE")));
        graphql(createMutation("subtype: TRAIN vehicleTypeKind: PHYSICAL trainElementType: CARRIAGE"))
                .body("errors[0].extensions.classification", equalTo("BAD_REQUEST"))
                .body("errors[0].message", org.hamcrest.Matchers.allOf(containsString("trainElementType"), containsString("TRAIN")));

        String trainId = createTrain("");
        graphql("mutation { createOrUpdateVehicleType(input: { netexId: \"" + trainId + "\" transportMode: RAIL dataOwnerRef: \"AKT\" }) }")
                .body("errors[0].extensions.classification", equalTo("BAD_REQUEST"))
                .body("errors[0].message", containsString("cannot be changed"));
    }

    @Test
    @DisplayName("A train that is part of a compound train cannot be deactivated")
    void trainInUseCannotBeDeactivated() {
        String trainId = createTrain("");
        createVehicleType("subtype: COMPOUND_TRAIN vehicleTypeKind: PHYSICAL compoundTrainComponents: [ { train: { netexId: \"" + trainId + "\" } } ]");
        String version = graphql("{ vehicleTypes(filter: { netexIds: [\"" + trainId + "\"] dataOwnerRef: \"AKT\" vehicleTypeKind: PHYSICAL }) { content { version } } }")
                .extract().path("data.vehicleTypes.content[0].version");

        graphql("mutation { deactivateVehicleType(input: { netexId: \"" + trainId + "\" version: " + version + " deactivateAt: \"2099-01-01T00:00:00.000+0000\" }) { netexId } }")
                .body("errors[0].extensions.classification", equalTo("BAD_REQUEST"))
                .body("errors[0].message", containsString("still part of a compound train"));
    }

    private int countOf(String netexId, String subtype) {
        String subtypeFilter = subtype == null ? "" : " subtype: " + subtype;
        return graphql("{ vehicleTypes(filter: { netexIds: [\"" + netexId + "\"] dataOwnerRef: \"AKT\" vehicleTypeKind: PHYSICAL" + subtypeFilter + " }) { totalElements } }")
                .body("errors", nullValue())
                .extract().path("data.vehicleTypes.totalElements");
    }

    /** A physical train element of the given type (an element type of the schema, e.g. CARRIAGE). */
    private String createTrainElement(String elementType) {
        return createVehicleType("subtype: TRAIN_ELEMENT vehicleTypeKind: PHYSICAL trainElementType: " + elementType);
    }

    /** A physical train of two cars, with the given extra input fields (e.g. components). */
    private String createTrain(String extraFields) {
        return createVehicleType("subtype: TRAIN vehicleTypeKind: PHYSICAL numberOfCars: 2 trainSizeType: SHORT " + extraFields);
    }

    private String createVehicleType(String fields) {
        return graphql(createMutation(fields))
                .body("errors", nullValue())
                .extract().path("data.createOrUpdateVehicleType");
    }

    private static String createMutation(String fields) {
        return "mutation { createOrUpdateVehicleType(input: { name: { value: \"Train test\" lang: \"en\" } transportMode: RAIL "
                + fields + " dataOwnerRef: \"AKT\" }) }";
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

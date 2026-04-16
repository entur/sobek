/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package org.rutebanken.sobek.rest.graphql;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.rutebanken.sobek.SobekTestApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = SobekTestApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GraphQLPagedQueriesTest {

    @LocalServerPort int port;

    @BeforeAll
    void setUp() throws Exception {
        RestAssured.port = port;
        importFixture("/fixtures/vehicle-type-import-basic.xml");
    }

    private void importFixture(String path) throws Exception {
        try (InputStream in = getClass().getResourceAsStream(path)) {
            String xml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            given()
                .contentType("application/xml")
                .queryParam("importType", "ID_MATCH")
                .body(xml)
                .when()
                .post("/services/vehicles/netex")
                .then()
                .statusCode(200);
        }
    }

    private String gql(String query) {
        return "{\"query\": \"" + query.replace("\"", "\\\"").replace("\n", " ") + "\"}";
    }

    // --- vehicleTypes paged query ---

    @Test
    void vehicleTypes_returnsPageStructure() {
        given()
            .contentType(ContentType.JSON)
            .body(gql("{ vehicleTypes(page: 0, size: 10) { content { id } totalElements page size } }"))
        .when()
            .post("/services/vehicles/graphql")
        .then()
            .statusCode(200)
            .body("data.vehicleTypes.content", is(not(empty())))
            .body("data.vehicleTypes.totalElements", greaterThanOrEqualTo(1))
            .body("data.vehicleTypes.page", equalTo(0))
            .body("data.vehicleTypes.size", equalTo(10));
    }

    @Test
    void vehicleTypes_exposesNewFields() {
        given()
            .contentType(ContentType.JSON)
            .body(gql("{ vehicleTypes(filter: { transportMode: bus }, page: 0, size: 10) { content { id name { value lang } shortName { value } transportMode created changed changedBy versionComment version } totalElements } }"))
        .when()
            .post("/services/vehicles/graphql")
        .then()
            .statusCode(200)
            .body("data.vehicleTypes.content[0].name.value", equalTo("Exqui City 24"))
            .body("data.vehicleTypes.content[0].transportMode", equalTo("bus"))
            .body("data.vehicleTypes.content[0].version", notNullValue())
            // created may be null for NeTEx-imported entities (set by versioning, not import)
            .body("data.vehicleTypes.content[0]", hasKey("created"));
    }

    @Test
    void vehicleTypes_filterByTransportMode() {
        given()
            .contentType(ContentType.JSON)
            .body(gql("{ vehicleTypes(filter: { transportMode: bus }, page: 0, size: 10) { content { id transportMode } totalElements } }"))
        .when()
            .post("/services/vehicles/graphql")
        .then()
            .statusCode(200)
            .body("data.vehicleTypes.content", is(not(empty())))
            .body("data.vehicleTypes.content.transportMode", everyItem(equalTo("bus")));
    }

    @Test
    void vehicleTypes_filterByTransportMode_noMatch() {
        given()
            .contentType(ContentType.JSON)
            .body(gql("{ vehicleTypes(filter: { transportMode: rail }, page: 0, size: 10) { content { id } totalElements } }"))
        .when()
            .post("/services/vehicles/graphql")
        .then()
            .statusCode(200)
            .body("data.vehicleTypes.content", is(empty()))
            .body("data.vehicleTypes.totalElements", equalTo(0));
    }

    @Test
    void vehicleTypes_filterByIds() {
        // First, get the actual NeTEx ID of the imported vehicle type
        String id = given()
            .contentType(ContentType.JSON)
            .body(gql("{ vehicleTypes(page: 0, size: 1) { content { id } } }"))
        .when()
            .post("/services/vehicles/graphql")
        .then()
            .statusCode(200)
            .body("data.vehicleTypes.content", hasSize(1))
            .extract().path("data.vehicleTypes.content[0].id");

        // Filter by that ID — should return exactly one result
        given()
            .contentType(ContentType.JSON)
            .body(gql("{ vehicleTypes(filter: { ids: [\"" + id + "\"] }, page: 0, size: 10) { content { id } totalElements } }"))
        .when()
            .post("/services/vehicles/graphql")
        .then()
            .statusCode(200)
            .body("data.vehicleTypes.content", hasSize(1))
            .body("data.vehicleTypes.content[0].id", equalTo(id))
            .body("data.vehicleTypes.totalElements", equalTo(1));
    }

    @Test
    void vehicleTypes_filterByIds_noMatch() {
        given()
            .contentType(ContentType.JSON)
            .body(gql("{ vehicleTypes(filter: { ids: [\"FAKE:VehicleType:999\"] }, page: 0, size: 10) { content { id } totalElements } }"))
        .when()
            .post("/services/vehicles/graphql")
        .then()
            .statusCode(200)
            .body("data.vehicleTypes.content", is(empty()))
            .body("data.vehicleTypes.totalElements", equalTo(0));
    }

    @Test
    void vehicleTypes_paginationBeyondResults() {
        given()
            .contentType(ContentType.JSON)
            .body(gql("{ vehicleTypes(page: 999, size: 10) { content { id } totalElements page } }"))
        .when()
            .post("/services/vehicles/graphql")
        .then()
            .statusCode(200)
            .body("data.vehicleTypes.content", is(empty()))
            .body("data.vehicleTypes.totalElements", greaterThanOrEqualTo(0))
            .body("data.vehicleTypes.page", equalTo(999));
    }

    // --- deckPlans paged query ---

    @Test
    void deckPlans_returnsPageStructure() {
        given()
            .contentType(ContentType.JSON)
            .body(gql("{ deckPlans(page: 0, size: 10) { content { id } totalElements page size } }"))
        .when()
            .post("/services/vehicles/graphql")
        .then()
            .statusCode(200)
            .body("data.deckPlans.totalElements", greaterThanOrEqualTo(0))
            .body("data.deckPlans.page", equalTo(0))
            .body("data.deckPlans.size", equalTo(10));
    }

    @Test
    void deckPlans_defaultPagination() {
        given()
            .contentType(ContentType.JSON)
            .body(gql("{ deckPlans { content { id } totalElements page size } }"))
        .when()
            .post("/services/vehicles/graphql")
        .then()
            .statusCode(200)
            .body("data.deckPlans.page", equalTo(0))
            .body("data.deckPlans.size", equalTo(20));
    }
}

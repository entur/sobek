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

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.rutebanken.sobek.SobekTestApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = SobekTestApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GraphQLMutationsTest {

    @LocalServerPort
    int port;

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

    private String getFixtureContents(String path) {
        try (InputStream in = getClass().getResourceAsStream(path)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String gql(String query) {
        return "{\"query\": \"" + query.replace("\"", "\\\"").replace("\n", " ") + "\"}";
    }

    // --- vehicleTypes paged query ---

    @Test
    void vehicle_mutationDummyVehicle() {

        // Create a dummy vehicle
        String resultId = given()
                .contentType(ContentType.JSON)
                .body(getFixtureContents("/fixtures/GraphQL_VehicleMutation1.QL"))
                .when()
                .post("/services/vehicles/graphql")
                .then()
                .statusCode(200)
                .body("data.createOrUpdateVehicle", is(not(empty())))
                .extract().path("data.createOrUpdateVehicle");

        // Verify that the dummy vehicle was created and check some of it's data
        given()
                .contentType(ContentType.JSON)
                .body(gql("{ vehicles(page: 0, size: 10, filter: { netexIds: [ " + resultId + " ]  }) { content { netexId, registrationNumber, chassisNumber } totalElements page size } }"))
                .when()
                .post("/services/vehicles/graphql")
                .then()
                .statusCode(200)
                .body("data.vehicles.content", is(not(empty())))
                .body("data.vehicles.totalElements", greaterThanOrEqualTo(1))
                .body("data.vehicleTypes.page", equalTo(0))
                .body("data.vehicleTypes.size", equalTo(10));
    }

}
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
import static org.assertj.core.api.Assertions.assertThat;
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
        return "{\"query\": \"" + sanitizeJson(query) + "\"}";
    }

    private String sanitizeJson(String json) {
        if (json == null) {
            return null;
        }

        // Replace common control characters with escaped versions
        return json.replace("\r\n", "\\n")
                .replace("\r", "\\n")
                .replace("\n", "\\n")
                .replace("\t", "\\t")
                .replace("\"", "\\\"")
                // Remove other control characters (0x00-0x1F except those above)
                .replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
    }
    // --- vehicleTypes paged query ---

    @Test
    void vehicle_mutationDummyVehicle() {

        // Create a dummy vehicle
        String resultId = given()
                .contentType(ContentType.JSON)
                .body(gql(getFixtureContents("/fixtures/GraphQL_VehicleMutation1.QL")))
                .when()
                .post("/services/vehicles/graphql")
                .then()
                .statusCode(200)
                .body("errors", nullValue())
                .extract().path("data.createOrUpdateVehicle");

        assertThat(resultId).isNotNull().isNotEmpty();

        // Verify that the dummy vehicle was created and check some of it's data
        given()
                .contentType(ContentType.JSON)
                .body(gql("{ vehicles(page: 0, size: 10, filter: { netexIds: [ \"" + resultId + "\" ]  }) { content { netexId, registrationNumber, chassisNumber } totalElements page size } }"))
                .when()
                .post("/services/vehicles/graphql")
                .then()
                .statusCode(200)
                .body("data.vehicles.content", is(not(empty())))
                .body("data.vehicles.totalElements", equalTo(1));
    }

    @Test
    void vehicle_mutationDummyVehicle_ShouldFail() {

        // Create a dummy vehicle
        given()
                .contentType(ContentType.JSON)
                .body(gql(getFixtureContents("/fixtures/GraphQL_VehicleMutation1.QL").replace("AKT:VehicleType:123", "FAKE:VehicleType:123")))
                .when()
                .post("/services/vehicles/graphql")
                .then()
                .statusCode(200)
                .body("errors", notNullValue());
    }
}
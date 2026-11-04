package org.rutebanken.sobek.rest.netex;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Who assigns ids, by the codespace of the id a client sends (through GraphQL and through NeTEx import):
 * <ul>
 *   <li>NMR and CVT are Sobek's: Sobek is the only one that assigns new ids there. An id that exists is an update.</li>
 *   <li>Codespaces in the valid list (e.g. AKT): the client's id is respected and used.</li>
 *   <li>Any other codespace (e.g. Autosys): Sobek assigns an id of its own, and the external id becomes imported-id.</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(SingleOrganisationAuthorizationConfiguration.class)
class NetexIdAssignmentIT {

    private static final String SOBEK_ONLY_MESSAGE = "assigned by Sobek";
    private static final Pattern IMPORTED_VEHICLE_TYPE_ID = Pattern.compile("<VehicleType\\s[^>]*?\\bid=\"([^\"]+)\"");

    @LocalServerPort int port;

    @Autowired private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    // --- Sobek's codespaces: only Sobek assigns new ids ---

    @Test
    void graphQl_newVehicleTypeWithIdInNmr_isRejected() {
        String id = "NMR:VehicleType:" + unique();

        graphQlMutationVehicleType(id, "PHYSICAL", "Rejected")
                .body("errors[0].extensions.classification", org.hamcrest.Matchers.equalTo("BAD_REQUEST"))
                .body("errors[0].message", org.hamcrest.Matchers.containsString(SOBEK_ONLY_MESSAGE));
        assertThat(rows("vehicle_type", id)).isZero();
    }

    @Test
    void graphQl_newContractualVehicleTypeWithIdInCvt_isRejected() {
        String id = "CVT:VehicleType:" + unique();

        graphQlMutationVehicleType(id, "CONTRACTUAL", "Rejected")
                .body("errors[0].extensions.classification", org.hamcrest.Matchers.equalTo("BAD_REQUEST"))
                .body("errors[0].message", org.hamcrest.Matchers.containsString(SOBEK_ONLY_MESSAGE));
        assertThat(rows("vehicle_type", id)).isZero();
    }

    @Test
    void import_newVehicleTypeWithIdInNmr_isRejected() throws Exception {
        String id = "NMR:VehicleType:" + unique();

        String body = importExpecting(400, "/fixtures/vehicle-type-import-with-id.xml", id);

        assertThat(body).contains(SOBEK_ONLY_MESSAGE);
        assertThat(rows("vehicle_type", id)).isZero();
    }

    @Test
    void import_newVehicleTypeWithIdInCvt_isRejected() throws Exception {
        String id = "CVT:VehicleType:" + unique();

        String body = importExpecting(400, "/fixtures/vehicle-type-import-with-id.xml", id);

        assertThat(body).contains(SOBEK_ONLY_MESSAGE);
        assertThat(rows("vehicle_type", id)).isZero();
    }

    @Test
    void import_malformedIdInCvt_isRejectedAndNeverBecomesContractual() throws Exception {
        for (String id : new String[] {"CVT:VehicleType:abc", "CVT:garbage", "CVT:VehicleType:1:2"}) {
            String body = importExpecting(400, "/fixtures/vehicle-type-import-with-id.xml", id);

            assertThat(body).as(id).contains(SOBEK_ONLY_MESSAGE);
            assertThat(rows("vehicle_type", id)).as(id).isZero();
        }
    }

    @Test
    void import_malformedIdInNmr_isRejected() throws Exception {
        for (String id : new String[] {"NMR:VehicleType:abc", "NMR:garbage"}) {
            String body = importExpecting(400, "/fixtures/vehicle-type-import-with-id.xml", id);

            assertThat(body).as(id).contains(SOBEK_ONLY_MESSAGE);
            assertThat(rows("vehicle_type", id)).as(id).isZero();
        }
    }

    @Test
    void graphQl_malformedIdInCvt_isRejected() {
        graphQlMutationVehicleType("CVT:VehicleType:abc", "CONTRACTUAL", "Rejected")
                .body("errors[0].extensions.classification", org.hamcrest.Matchers.equalTo("BAD_REQUEST"))
                .body("errors[0].message", org.hamcrest.Matchers.containsString(SOBEK_ONLY_MESSAGE));
    }

    @Test
    void import_newDeckPlanWithIdInNmr_isRejected() throws Exception {
        String id = "NMR:DeckPlan:" + unique();

        String body = importExpecting(400, "/fixtures/deck-plan-import-with-id.xml", id);

        assertThat(body).contains(SOBEK_ONLY_MESSAGE);
        assertThat(rows("deck_plan", id)).isZero();
    }

    // --- Sobek's codespaces: an id that exists is an update ---

    @Test
    void graphQl_existingIdInNmr_isAnUpdate() {
        String id = graphQlCreateVehicleType(null, "PHYSICAL", "Original name");
        assertThat(id).startsWith("NMR:VehicleType:");

        String updatedId = graphQlCreateVehicleType(id, "PHYSICAL", "Updated name");

        assertThat(updatedId).isEqualTo(id);
        assertThat(currentVehicleType(id, "PHYSICAL").get("version")).isEqualTo("2");
    }

    @Test
    void graphQl_existingIdInCvt_isAnUpdate() {
        String id = graphQlCreateVehicleType(null, "CONTRACTUAL", "Original name");
        assertThat(id).startsWith("CVT:VehicleType:");

        String updatedId = graphQlCreateVehicleType(id, "CONTRACTUAL", "Updated name");

        assertThat(updatedId).isEqualTo(id);
        assertThat(currentVehicleType(id, "CONTRACTUAL").get("version")).isEqualTo("2");
    }

    @Test
    void import_existingIdInNmr_isAnUpdate() throws Exception {
        String id = graphQlCreateVehicleType(null, "PHYSICAL", "Original name");

        String importedId = importVehicleType(id);

        assertThat(importedId).isEqualTo(id);
        assertThat(currentVehicleType(id, "PHYSICAL").get("version")).isEqualTo("2");
        assertThat(rows("vehicle_type", id)).as("rows, one per version").isEqualTo(2);
    }

    // --- registered codespaces: the client's id is respected ---

    @Test
    void graphQl_newObjectWithIdInARegisteredCodespace_usesThatId() {
        String id = "AKT:VehicleType:" + unique();

        String createdId = graphQlCreateVehicleType(id, "PHYSICAL", "Registered");

        assertThat(createdId).isEqualTo(id);
        assertThat(keyValues(id)).doesNotContainKey("imported-id");

        // And the same id again is an update of it
        assertThat(graphQlCreateVehicleType(id, "PHYSICAL", "Registered again")).isEqualTo(id);
        assertThat(currentVehicleType(id, "PHYSICAL").get("version")).isEqualTo("2");
    }

    @Test
    void import_newObjectWithIdInARegisteredCodespace_usesThatId() throws Exception {
        String id = "AKT:VehicleType:" + unique();

        assertThat(importVehicleType(id)).isEqualTo(id);
        assertThat(rows("vehicle_type", id)).isEqualTo(1);
        assertThat(keyValues(id)).doesNotContainKey("imported-id");
    }

    // --- other codespaces: Sobek assigns the id, the external id is kept as imported-id ---

    @Test
    void graphQl_newObjectWithAnExternalId_getsSobeksIdAndKeepsTheExternalOneAsImportedId() {
        String externalId = "EXT:VehicleType:" + unique();

        String id = graphQlCreateVehicleType(externalId, "PHYSICAL", "External");

        assertThat(id).startsWith("NMR:VehicleType:").isNotEqualTo(externalId);
        assertThat(keyValues(id)).containsEntry("imported-id", externalId);
        assertThat(rows("vehicle_type", externalId)).as("rows with the external id as id").isZero();

        // Sending the external id again is an update of the same object
        assertThat(graphQlCreateVehicleType(externalId, "PHYSICAL", "External again")).isEqualTo(id);
        assertThat(currentVehicleType(id, "PHYSICAL").get("version")).isEqualTo("2");
    }

    @Test
    void graphQl_newContractualObjectWithAnExternalId_getsAnIdInTheContractualCodespace() {
        String externalId = "EXT:VehicleType:" + unique();

        String id = graphQlCreateVehicleType(externalId, "CONTRACTUAL", "External contractual");

        assertThat(id).startsWith("CVT:VehicleType:");
        assertThat(keyValues(id)).containsEntry("imported-id", externalId);
    }

    @Test
    void import_newObjectWithAnExternalId_getsSobeksIdAndKeepsTheExternalOneAsImportedId() throws Exception {
        String externalId = "EXT:VehicleType:" + unique();

        String id = importVehicleType(externalId);

        assertThat(id).startsWith("NMR:VehicleType:").isNotEqualTo(externalId);
        assertThat(keyValues(id)).containsEntry("imported-id", externalId);

        // Importing the external id again is an update of the same object
        assertThat(importVehicleType(externalId)).isEqualTo(id);
        assertThat(currentVehicleType(id, "PHYSICAL").get("version")).isEqualTo("2");
    }

    // --- the kind of imported vehicle types: NeTEx cannot say it, so the import asks for it ---

    @Test
    void import_withoutVehicleTypeKind_isRejected() throws Exception {
        String body = importExpecting(400, "/fixtures/vehicle-type-import-with-id.xml", "AKT:VehicleType:" + unique(), null);

        assertThat(body).contains("vehicleTypeKind is required");
    }

    @Test
    void import_withAnInvalidVehicleTypeKind_isRejected() throws Exception {
        String body = importExpecting(400, "/fixtures/vehicle-type-import-with-id.xml", "AKT:VehicleType:" + unique(), "BOTH");

        assertThat(body).contains("Invalid value 'BOTH'").contains("PHYSICAL, CONTRACTUAL");
    }

    @Test
    void import_contractualKind_createsAContractualTypeInARegisteredCodespace() throws Exception {
        String id = "AKT:VehicleType:" + unique();

        assertThat(importVehicleType(id, "CONTRACTUAL")).isEqualTo(id);

        assertThat(currentVehicleType(id, "CONTRACTUAL").get("netexId")).isEqualTo(id);
        assertThat(jdbcTemplate.queryForObject("SELECT vehicle_type_kind FROM vehicle_type WHERE netex_id = ?", String.class, id))
                .isEqualTo("CONTRACTUAL");
    }

    @Test
    void import_contractualKind_withAnExternalId_getsAContractualIdFromSobek() throws Exception {
        // An external id says nothing about the kind, so the kind asked for applies and Sobek assigns an id in CVT
        String externalId = "EXT:VehicleType:" + unique();

        String id = importVehicleType(externalId, "CONTRACTUAL");

        assertThat(id).startsWith("CVT:VehicleType:");
        assertThat(keyValues(id)).containsEntry("imported-id", externalId);
    }

    @Test
    void import_kindDifferentFromTheExistingType_isRejected() throws Exception {
        String id = "AKT:VehicleType:" + unique();
        importVehicleType(id, "PHYSICAL");

        String body = importExpecting(400, "/fixtures/vehicle-type-import-with-id.xml", id, "CONTRACTUAL");

        assertThat(body).contains("cannot be changed");
        assertThat(currentVehicleType(id, "PHYSICAL").get("version")).isEqualTo("1");
    }

    @Test
    void import_idInTheContractualCodespaceIsContractualWhateverKindIsAsked() throws Exception {
        String id = graphQlCreateVehicleType(null, "CONTRACTUAL", "Original name");
        assertThat(id).startsWith("CVT:VehicleType:");

        assertThat(importVehicleType(id, "PHYSICAL")).isEqualTo(id);

        assertThat(currentVehicleType(id, "CONTRACTUAL").get("version")).isEqualTo("2");
    }

    // --- helpers ---

    private static long unique() {
        return 800_000_000L + (System.nanoTime() % 100_000_000L);
    }

    private long rows(String table, String netexId) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE netex_id = ?", Long.class, netexId);
    }

    /** The id the vehicle type has in the response of an import. */
    private String importVehicleType(String idInImport) throws Exception {
        return importVehicleType(idInImport, "PHYSICAL");
    }

    private String importVehicleType(String idInImport, String kind) throws Exception {
        String body = importExpecting(200, "/fixtures/vehicle-type-import-with-id.xml", idInImport, kind);
        Matcher matcher = IMPORTED_VEHICLE_TYPE_ID.matcher(body);
        assertThat(matcher.find()).as("a VehicleType in the response").isTrue();
        return matcher.group(1);
    }

    private String importExpecting(int status, String fixture, String id) throws Exception {
        return importExpecting(status, fixture, id, "PHYSICAL");
    }

    private String importExpecting(int status, String fixture, String id, String kind) throws Exception {
        String xml;
        try (InputStream in = getClass().getResourceAsStream(fixture)) {
            xml = new String(in.readAllBytes(), StandardCharsets.UTF_8).replace("@ID@", id);
        }
        io.restassured.response.Response response = given()
                .contentType("application/xml")
                .body(xml)
                .when()
                .post("/services/vehicles/netex" + (kind == null ? "" : "?vehicleTypeKind=" + kind));
        assertThat(response.statusCode()).as("status of importing id %s: %s", id, response.body().asString()).isEqualTo(status);
        return response.body().asString();
    }

    private String graphQlCreateVehicleType(String netexId, String kind, String name) {
        return graphQlMutationVehicleType(netexId, kind, name)
                .body("errors", org.hamcrest.Matchers.nullValue())
                .extract().path("data.createOrUpdateVehicleType");
    }

    private io.restassured.response.ValidatableResponse graphQlMutationVehicleType(String netexId, String kind, String name) {
        return graphQl("mutation { createOrUpdateVehicleType(input: { "
                + (netexId == null ? "" : "netexId: \"" + netexId + "\" ")
                + "name: { value: \"" + name + "\" lang: \"en\" } transportMode: BUS "
                + "vehicleTypeKind: " + kind + " dataOwnerRef: \"AKT\" }) }");
    }

    private Map<String, Object> currentVehicleType(String netexId, String kind) {
        JsonPath json = graphQl("{ vehicleTypes(filter: { netexIds: [\"" + netexId + "\"] dataOwnerRef: \"AKT\" vehicleTypeKind: " + kind + " }) "
                + "{ content { netexId version } totalElements } }")
                .extract().jsonPath();
        assertThat(json.getInt("data.vehicleTypes.totalElements")).as("current vehicle types with id %s", netexId).isEqualTo(1);
        return json.getMap("data.vehicleTypes.content[0]");
    }

    /**
     * The key values of the latest version of the vehicle type. Read with SQL, as the GraphQL field keyValues
     * cannot be resolved for a vehicle type (the collection is lazy and the session is gone).
     */
    private Map<String, String> keyValues(String netexId) {
        Map<String, String> result = new java.util.HashMap<>();
        jdbcTemplate.query(
                "SELECT kv.key, kv.value FROM vehicle_type vt"
                        + " JOIN vehicle_type_key_values j ON j.vehicle_type_id = vt.id"
                        + " JOIN key_value kv ON kv.id = j.key_values_id"
                        + " WHERE vt.netex_id = ? AND vt.version = (SELECT MAX(version) FROM vehicle_type WHERE netex_id = ?)",
                rs -> {
                    result.put(rs.getString(1), rs.getString(2));
                },
                netexId, netexId);
        return result;
    }

    private io.restassured.response.ValidatableResponse graphQl(String query) {
        return given()
                .contentType(ContentType.JSON)
                .body("{\"query\": \"" + query.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}")
                .when()
                .post("/services/vehicles/graphql")
                .then()
                .statusCode(200);
    }
}

package org.rutebanken.sobek.rest.graphql;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.rutebanken.sobek.config.JerseyConfig.SERVICES_VEHICLES_PATH;

import io.restassured.RestAssured;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rutebanken.sobek.SobekAppTestApplication;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;
import org.rutebanken.sobek.model.vehicle.DeckPlan;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.versioning.save.DeckPlanVersionedSaverService;
import org.rutebanken.sobek.versioning.save.VehicleTypeVersionedSaverService;
import org.rutebanken.sobek.versioning.save.VehicleVersionedSaverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for GraphQL schema issues documented in GitHub issue #48. These tests assert
 * CORRECT behavior and are expected to FAIL until the corresponding fixes are applied.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = SobekAppTestApplication.class)
@ActiveProfiles({"test", "local-blobstore"})
class GraphQLSchemaIT {

  private static final String GRAPHQL_PATH = SERVICES_VEHICLES_PATH + "/graphql";

  @Autowired private VehicleVersionedSaverService vehicleVersionedSaverService;
  @Autowired private VehicleTypeVersionedSaverService vehicleTypeVersionedSaverService;
  @Autowired private DeckPlanVersionedSaverService deckPlanVersionedSaverService;

  @Value("${local.server.port}")
  private int port;

  @BeforeEach
  void setUp() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = port;
  }

  /**
   * Issue #48 item 1+2: VehicleType.vehicles should return the associated vehicles.
   *
   * <p>Currently broken because:
   *
   * <ul>
   *   <li>VehicleTypeVehicleFetcher returns getDeckPlan() instead of getVehicles()
   *   <li>The fetcher is never registered in buildCodeRegistry(), so the field falls through to the
   *       default property resolver
   * </ul>
   */
  @Test
  @DisplayName("#48.1+2: VehicleType.vehicles returns actual vehicles, not DeckPlan")
  void vehicleType_vehicles_shouldReturnAssociatedVehicles() {
    DeckPlan deckPlan = new DeckPlan();
    deckPlan.setName(new EmbeddableMultilingualString("Test Deck Plan"));
    deckPlan = deckPlanVersionedSaverService.saveNewVersion(deckPlan);

    VehicleType vehicleType = new VehicleType();
    vehicleType.setName(new EmbeddableMultilingualString("Test Vehicle Type"));
    vehicleType.setDeckPlan(deckPlan);
    vehicleType = vehicleTypeVersionedSaverService.saveNewVersion(vehicleType);

    Vehicle vehicle = new Vehicle();
    vehicle.setName(new EmbeddableMultilingualString("Test Vehicle"));
    vehicle.setRegistrationNumber("REG-001");
    vehicle.setTransportType(vehicleType);
    vehicleVersionedSaverService.saveNewVersion(vehicle);

    String query =
        """
        {
          vehicleTypes {
            id
            vehicles {
              id
              registrationNumber
            }
          }
        }
        """;

    Map<String, Object> response = executeGraphQL(query);

    Map<String, Object> data = getData(response);
    List<Map<String, Object>> vehicleTypes = getList(data, "vehicleTypes");
    assertThat(vehicleTypes).isNotEmpty();

    Map<String, Object> returnedType = vehicleTypes.get(0);
    List<Map<String, Object>> vehicles = getList(returnedType, "vehicles");

    assertThat(vehicles)
        .as("vehicles field should return actual Vehicle entities, not null or DeckPlan")
        .isNotNull()
        .isNotEmpty();

    Map<String, Object> returnedVehicle = vehicles.get(0);
    assertThat(returnedVehicle.get("registrationNumber"))
        .as("returned vehicle should have the correct registration number")
        .isEqualTo("REG-001");
  }

  /**
   * Issue #48 item 5: Vehicle.id should return the netexId, not the database ID.
   *
   * <p>DeckPlan and VehicleType have getNetexIdFetcher() registered for their id field, but Vehicle
   * does not — it relies on the default property resolver which returns the internal database Long
   * ID.
   */
  @Test
  @DisplayName("#48.5: Vehicle.id returns netexId, not database Long ID")
  void vehicle_id_shouldReturnNetexId() {
    VehicleType vehicleType = new VehicleType();
    vehicleType.setName(new EmbeddableMultilingualString("Type for ID test"));
    vehicleType = vehicleTypeVersionedSaverService.saveNewVersion(vehicleType);

    Vehicle vehicle = new Vehicle();
    vehicle.setName(new EmbeddableMultilingualString("Vehicle for ID test"));
    vehicle.setTransportType(vehicleType);
    vehicle = vehicleVersionedSaverService.saveNewVersion(vehicle);

    String expectedNetexId = vehicle.getNetexId();

    String query =
        """
        {
          vehicleTypes {
            vehicles {
              id
            }
          }
        }
        """;

    Map<String, Object> response = executeGraphQL(query);

    Map<String, Object> data = getData(response);
    List<Map<String, Object>> vehicleTypes = getList(data, "vehicleTypes");
    assertThat(vehicleTypes).isNotEmpty();

    List<Map<String, Object>> vehicles = getList(vehicleTypes.get(0), "vehicles");
    assertThat(vehicles).isNotNull().isNotEmpty();

    String returnedId = String.valueOf(vehicles.get(0).get("id"));
    assertThat(returnedId)
        .as("Vehicle id should be the netexId (e.g. NMR:Vehicle:1), not the database Long ID")
        .isEqualTo(expectedNetexId)
        .startsWith("NMR:");
  }

  /**
   * Issue #48 item 3: VehicleType dimensions should preserve decimal precision.
   *
   * <p>length, width, height are BigDecimal in the JPA model but mapped to GraphQLInt, silently
   * truncating values like 12.5 to 12.
   */
  @Test
  @DisplayName("#48.3: VehicleType length/width/height preserve decimal precision")
  void vehicleType_dimensions_shouldPreserveDecimalPrecision() {
    VehicleType vehicleType = new VehicleType();
    vehicleType.setName(new EmbeddableMultilingualString("Dimension test type"));
    vehicleType.setLength(new BigDecimal("12.5"));
    vehicleType.setWidth(new BigDecimal("3.2"));
    vehicleType.setHeight(new BigDecimal("4.7"));
    vehicleTypeVersionedSaverService.saveNewVersion(vehicleType);

    String query =
        """
        {
          vehicleTypes {
            id
            length
            width
            height
          }
        }
        """;

    Map<String, Object> response = executeGraphQL(query);

    // The query itself should succeed without errors
    assertThat(response.get("errors"))
        .as("querying decimal dimensions should not produce GraphQL errors")
        .isNull();

    Map<String, Object> data = getData(response);
    assertThat(data).as("response should contain data").isNotNull();

    List<Map<String, Object>> vehicleTypes = getList(data, "vehicleTypes");
    assertThat(vehicleTypes).isNotEmpty();

    Map<String, Object> returned = vehicleTypes.get(0);

    assertThat(((Number) returned.get("length")).doubleValue())
        .as("length should preserve decimal: 12.5, not truncate to 12")
        .isEqualTo(12.5);
    assertThat(((Number) returned.get("width")).doubleValue())
        .as("width should preserve decimal: 3.2, not truncate to 3")
        .isEqualTo(3.2);
    assertThat(((Number) returned.get("height")).doubleValue())
        .as("height should preserve decimal: 4.7, not truncate to 4")
        .isEqualTo(4.7);
  }

  /**
   * Issue #48 item 4: Vehicle.description should return the value string, not
   * EmbeddableMultilingualString.toString().
   *
   * <p>description is EmbeddableMultilingualString in the JPA model but mapped as plain
   * GraphQLString. The default resolver returns the object, and GraphQL coerces it via toString()
   * producing garbage like "EmbeddableMultilingualString(value=..., lang=...)".
   */
  @Test
  @DisplayName("#48.4: Vehicle.description returns plain value, not toString() garbage")
  void vehicle_description_shouldReturnPlainValue() {
    VehicleType vehicleType = new VehicleType();
    vehicleType.setName(new EmbeddableMultilingualString("Type for desc test"));
    vehicleType = vehicleTypeVersionedSaverService.saveNewVersion(vehicleType);

    Vehicle vehicle = new Vehicle();
    vehicle.setName(new EmbeddableMultilingualString("Vehicle for desc test"));
    vehicle.setDescription(new EmbeddableMultilingualString("A test description", "en"));
    vehicle.setTransportType(vehicleType);
    vehicleVersionedSaverService.saveNewVersion(vehicle);

    String query =
        """
        {
          vehicleTypes {
            vehicles {
              description
            }
          }
        }
        """;

    Map<String, Object> response = executeGraphQL(query);

    assertThat(response.get("errors")).as("query should not produce errors").isNull();

    Map<String, Object> data = getData(response);
    List<Map<String, Object>> vehicleTypes = getList(data, "vehicleTypes");
    assertThat(vehicleTypes).isNotEmpty();

    List<Map<String, Object>> vehicles = getList(vehicleTypes.get(0), "vehicles");
    assertThat(vehicles).isNotNull().isNotEmpty();

    Object description = vehicles.get(0).get("description");
    assertThat(description)
        .as("description should be the plain value string, not EmbeddableMultilingualString.toString()")
        .isEqualTo("A test description");
  }

  /**
   * Issue #48 item 4 (DeckPlan): Same description type mismatch applies to DeckPlan.
   */
  @Test
  @DisplayName("#48.4: DeckPlan.description returns plain value, not toString() garbage")
  void deckPlan_description_shouldReturnPlainValue() {
    DeckPlan deckPlan = new DeckPlan();
    deckPlan.setName(new EmbeddableMultilingualString("Deck plan desc test"));
    deckPlan.setDescription(new EmbeddableMultilingualString("Deck plan description", "no"));
    deckPlan = deckPlanVersionedSaverService.saveNewVersion(deckPlan);

    VehicleType vehicleType = new VehicleType();
    vehicleType.setName(new EmbeddableMultilingualString("Type with deck plan"));
    vehicleType.setDeckPlan(deckPlan);
    vehicleTypeVersionedSaverService.saveNewVersion(vehicleType);

    String query =
        """
        {
          vehicleTypes {
            deckPlan {
              description
            }
          }
        }
        """;

    Map<String, Object> response = executeGraphQL(query);

    assertThat(response.get("errors")).as("query should not produce errors").isNull();

    Map<String, Object> data = getData(response);
    List<Map<String, Object>> vehicleTypes = getList(data, "vehicleTypes");
    assertThat(vehicleTypes).isNotEmpty();

    Map<String, Object> returnedDeckPlan = getMap(vehicleTypes.get(0), "deckPlan");
    assertThat(returnedDeckPlan).isNotNull();

    Object description = returnedDeckPlan.get("description");
    assertThat(description)
        .as("description should be the plain value string, not EmbeddableMultilingualString.toString()")
        .isEqualTo("Deck plan description");
  }

  // --- helpers ---

  @SuppressWarnings("unchecked")
  private Map<String, Object> executeGraphQL(String query) {
    Map<String, Object> body = new HashMap<>();
    body.put("query", query);

    return given()
        .contentType("application/json")
        .body(body)
        .when()
        .post(GRAPHQL_PATH)
        .then()
        .extract()
        .as(Map.class);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getData(Map<String, Object> response) {
    return (Map<String, Object>) response.get("data");
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> getList(Map<String, Object> parent, String key) {
    return (List<Map<String, Object>>) parent.get(key);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getMap(Map<String, Object> parent, String key) {
    return (Map<String, Object>) parent.get(key);
  }
}

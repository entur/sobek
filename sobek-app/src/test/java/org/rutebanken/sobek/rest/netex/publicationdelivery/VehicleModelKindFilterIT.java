package org.rutebanken.sobek.rest.netex.publicationdelivery;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;
import org.rutebanken.sobek.model.vehicle.AllPublicTransportModesEnumeration;
import org.rutebanken.sobek.model.vehicle.DeckPlan;
import org.rutebanken.sobek.model.vehicle.VehicleModel;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.model.vehicle.VehicleTypeKindEnumeration;
import org.rutebanken.sobek.repository.VehicleModelRepository;
import org.rutebanken.sobek.rest.netex.SingleOrganisationAuthorizationConfiguration;
import org.rutebanken.sobek.versioning.save.DeckPlanVersionedSaverService;
import org.rutebanken.sobek.versioning.save.VehicleModelVersionedSaverService;
import org.rutebanken.sobek.versioning.save.VehicleTypeVersionedSaverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;

/**
 * A vehicle model follows the kind of its vehicle type: the repository query and the NeTEx export
 * only return the models of the requested kind.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(SingleOrganisationAuthorizationConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VehicleModelKindFilterIT {

    private static final String DATA_OWNER = SingleOrganisationAuthorizationConfiguration.DATA_OWNER;

    @LocalServerPort int port;

    @Autowired private VehicleTypeVersionedSaverService vehicleTypeSaver;
    @Autowired private VehicleModelVersionedSaverService vehicleModelSaver;
    @Autowired private DeckPlanVersionedSaverService deckPlanSaver;
    @Autowired private VehicleModelRepository vehicleModelRepository;

    private String physicalModel;
    private String contractualModel;
    private String physicalDeckPlan;
    private String contractualDeckPlan;
    private String unconnectedDeckPlan;

    @BeforeAll
    void seed() {
        DeckPlan physicalPlan = saveDeckPlan("Kind filter physical deck plan");
        DeckPlan contractualPlan = saveDeckPlan("Kind filter contractual deck plan");
        physicalDeckPlan = physicalPlan.getNetexId();
        contractualDeckPlan = contractualPlan.getNetexId();
        unconnectedDeckPlan = saveDeckPlan("Kind filter unconnected deck plan").getNetexId();

        physicalModel = saveModelOf(VehicleTypeKindEnumeration.PHYSICAL, "Model kind filter physical", physicalPlan);
        contractualModel = saveModelOf(VehicleTypeKindEnumeration.CONTRACTUAL, "Model kind filter contractual", contractualPlan);
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void repository_physical_returnsOnlyModelsOfPhysicalTypes() {
        List<String> ids = modelIds(VehicleTypeKindEnumeration.PHYSICAL);

        assertThat(ids).contains(physicalModel).doesNotContain(contractualModel);
    }

    @Test
    void repository_contractual_returnsOnlyModelsOfContractualTypes() {
        List<String> ids = modelIds(VehicleTypeKindEnumeration.CONTRACTUAL);

        assertThat(ids).contains(contractualModel).doesNotContain(physicalModel);
    }

    @Test
    void repository_withoutKind_returnsModelsOfBothKinds() {
        List<String> ids = modelIds(null);

        assertThat(ids).contains(physicalModel, contractualModel);
    }

    @Test
    void export_physical_containsOnlyModelsOfPhysicalTypes() {
        String xml = export("PHYSICAL");

        assertThat(xml).contains(idAttribute(physicalModel)).doesNotContain(idAttribute(contractualModel));
    }

    @Test
    void export_contractual_containsOnlyModelsOfContractualTypes() {
        String xml = export("CONTRACTUAL");

        assertThat(xml).contains(idAttribute(contractualModel)).doesNotContain(idAttribute(physicalModel));
    }

    @Test
    void export_both_containsModelsOfBothKinds() {
        String xml = export("BOTH");

        assertThat(xml).contains(idAttribute(physicalModel), idAttribute(contractualModel));
    }

    @Test
    void export_physical_containsOnlyDeckPlansConnectedToPhysicalTypes() {
        String xml = export("PHYSICAL");

        assertThat(xml).contains(idAttribute(physicalDeckPlan))
                .doesNotContain(idAttribute(contractualDeckPlan))
                .doesNotContain(idAttribute(unconnectedDeckPlan));
    }

    @Test
    void export_contractual_containsOnlyDeckPlansConnectedToContractualTypes() {
        String xml = export("CONTRACTUAL");

        assertThat(xml).contains(idAttribute(contractualDeckPlan))
                .doesNotContain(idAttribute(physicalDeckPlan))
                .doesNotContain(idAttribute(unconnectedDeckPlan));
    }

    @Test
    void export_both_doesNotFilterDeckPlansOnKind() {
        String xml = export("BOTH");

        assertThat(xml).contains(idAttribute(physicalDeckPlan), idAttribute(contractualDeckPlan), idAttribute(unconnectedDeckPlan));
    }

    private List<String> modelIds(VehicleTypeKindEnumeration kind) {
        return vehicleModelRepository
                .findCurrentFiltered(DATA_OWNER, null, null, kind, null, Pageable.unpaged())
                .getContent().stream()
                .map(VehicleModel::getNetexId)
                .toList();
    }

    private static String export(String kindSelection) {
        return given()
                .accept(ContentType.XML)
                .queryParam("dataOwner", DATA_OWNER)
                .queryParam("vehicleTypeKind", kindSelection)
                .when()
                .get("/services/vehicles/netex")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();
    }

    /** Matches the model element itself, not references to it (those use ref="..."). */
    private static String idAttribute(String netexId) {
        return "id=\"" + netexId + "\"";
    }

    private DeckPlan saveDeckPlan(String name) {
        DeckPlan deckPlan = new DeckPlan();
        deckPlan.setName(new EmbeddableMultilingualString(name, "en"));
        deckPlan.setDataOwnerRef(DATA_OWNER);
        return deckPlanSaver.saveNewVersion(deckPlan);
    }

    /** Saves a vehicle type of the kind, connected to the deck plan, and its one vehicle model, through the real savers. */
    private String saveModelOf(VehicleTypeKindEnumeration kind, String name, DeckPlan deckPlan) {
        VehicleType type = new VehicleType();
        type.setName(new EmbeddableMultilingualString(name, "en"));
        type.setTransportMode(AllPublicTransportModesEnumeration.BUS);
        type.setDataOwnerRef(DATA_OWNER);
        type.setVehicleTypeKind(kind);
        type.setDeckPlan(deckPlan);
        VehicleType savedType = vehicleTypeSaver.saveNewVersion(type);

        VehicleModel model = new VehicleModel();
        model.setName(new EmbeddableMultilingualString(name, "en"));
        model.setTransportType(savedType);
        return vehicleModelSaver.saveNewVersion(model).getNetexId();
    }
}

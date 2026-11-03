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
import org.rutebanken.sobek.model.vehicle.VehicleModel;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.model.vehicle.VehicleTypeKindEnumeration;
import org.rutebanken.sobek.repository.VehicleModelRepository;
import org.rutebanken.sobek.rest.netex.SingleOrganisationAuthorizationConfiguration;
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
    @Autowired private VehicleModelRepository vehicleModelRepository;

    private String physicalModel;
    private String contractualModel;

    @BeforeAll
    void seed() {
        physicalModel = saveModelOf(VehicleTypeKindEnumeration.PHYSICAL, "Model kind filter physical");
        contractualModel = saveModelOf(VehicleTypeKindEnumeration.CONTRACTUAL, "Model kind filter contractual");
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

    /** Saves a vehicle type of the kind and its one vehicle model, through the real savers. */
    private String saveModelOf(VehicleTypeKindEnumeration kind, String name) {
        VehicleType type = new VehicleType();
        type.setName(new EmbeddableMultilingualString(name, "en"));
        type.setTransportMode(AllPublicTransportModesEnumeration.BUS);
        type.setDataOwnerRef(DATA_OWNER);
        type.setVehicleTypeKind(kind);
        VehicleType savedType = vehicleTypeSaver.saveNewVersion(type);

        VehicleModel model = new VehicleModel();
        model.setName(new EmbeddableMultilingualString(name, "en"));
        model.setTransportType(savedType);
        return vehicleModelSaver.saveNewVersion(model).getNetexId();
    }
}

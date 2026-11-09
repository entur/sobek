package org.rutebanken.sobek.rest.netex;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.validation.ValidationException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;
import org.rutebanken.sobek.model.vehicle.AllPublicTransportModesEnumeration;
import org.rutebanken.sobek.model.vehicle.Train;
import org.rutebanken.sobek.model.vehicle.TrainComponent;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.model.vehicle.VehicleModel;
import org.rutebanken.sobek.model.vehicle.VehicleTypeKindEnumeration;
import org.rutebanken.sobek.repository.VehicleModelRepository;
import org.rutebanken.sobek.repository.VehicleRepository;
import org.rutebanken.sobek.versioning.save.VehicleModelVersionedSaverService;
import org.rutebanken.sobek.versioning.save.VehicleTypeVersionedSaverService;
import org.rutebanken.sobek.versioning.save.VehicleVersionedSaverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * What the rest of the register does with a Train as the vehicle type of a vehicle or a vehicle model: the kind rules
 * and the contractual codespace apply to it, it follows a new version of the train, and it is exported.
 * The data is saved through the real savers, as the services do.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(SingleOrganisationAuthorizationConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TrainReferencesIT {

    private static final String DATA_OWNER = SingleOrganisationAuthorizationConfiguration.DATA_OWNER;

    @LocalServerPort int port;

    @Autowired private VehicleTypeVersionedSaverService vehicleTypeSaver;
    @Autowired private VehicleVersionedSaverService vehicleSaver;
    @Autowired private VehicleModelVersionedSaverService vehicleModelSaver;
    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private VehicleModelRepository vehicleModelRepository;
    @Autowired private TransactionTemplate transactionTemplate;

    private Train physicalTrain;
    private Train contractualTrain;
    private String vehicleOfPhysicalTrain;
    private String modelOfPhysicalTrain;

    @BeforeAll
    void seed() {
        physicalTrain = saveTrain(VehicleTypeKindEnumeration.PHYSICAL, "References physical train", 2);
        contractualTrain = saveTrain(VehicleTypeKindEnumeration.CONTRACTUAL, "References contractual train", 2);
        vehicleOfPhysicalTrain = saveVehicleOf(physicalTrain).getNetexId();
        modelOfPhysicalTrain = saveModelOf(physicalTrain).getNetexId();
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("A contractual train is in the contractual codespace and a physical one is not")
    void contractualTrainIsInTheContractualCodespace() {
        assertThat(contractualTrain.getNetexId()).startsWith("CVT:Train:");
        assertThat(physicalTrain.getNetexId()).doesNotStartWith("CVT:").contains(":Train:");
    }

    @Test
    @DisplayName("The export of a kind has the trains of that kind only")
    void exportHasOnlyTrainsOfTheRequestedKind() {
        String physical = export("PHYSICAL");
        String contractual = export("CONTRACTUAL");

        assertThat(physical).contains(idAttribute(physicalTrain.getNetexId())).doesNotContain(idAttribute(contractualTrain.getNetexId()));
        assertThat(contractual).contains(idAttribute(contractualTrain.getNetexId())).doesNotContain(idAttribute(physicalTrain.getNetexId()));
    }

    @Test
    @DisplayName("A vehicle can be of a physical train, and is exported with it")
    void vehicleOfATrainIsSavedAndExported() {
        Vehicle vehicle = vehicleRepository.findFirstByNetexIdOrderByVersionDesc(vehicleOfPhysicalTrain);

        assertThat(vehicle).isNotNull();
        assertThat(export("PHYSICAL")).contains(idAttribute(vehicleOfPhysicalTrain)).contains("ref=\"" + physicalTrain.getNetexId() + "\"");
    }

    @Test
    @DisplayName("A vehicle cannot be of a contractual train")
    void vehicleOfAContractualTrainIsRejected() {
        assertThatThrownBy(() -> saveVehicleOf(contractualTrain)).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("A vehicle model can be of a train, and follows its kind")
    void vehicleModelOfATrainFollowsItsKind() {
        List<String> physicalModels = modelIds(VehicleTypeKindEnumeration.PHYSICAL);
        List<String> contractualModels = modelIds(VehicleTypeKindEnumeration.CONTRACTUAL);

        assertThat(physicalModels).contains(modelOfPhysicalTrain);
        assertThat(contractualModels).doesNotContain(modelOfPhysicalTrain);
        assertThat(export("PHYSICAL")).contains(idAttribute(modelOfPhysicalTrain));
    }

    @Test
    @DisplayName("When a train gets a new version, its vehicles follow it")
    void vehiclesFollowANewVersionOfTheirTrain() {
        // Given: a train with a vehicle
        Train train = saveTrain(VehicleTypeKindEnumeration.PHYSICAL, "References train with new version", 2);
        Vehicle vehicle = saveVehicleOf(train);

        // When: the train gets a new version, with another size
        Train newVersion = trainWithoutId(VehicleTypeKindEnumeration.PHYSICAL, "References train with new version", 3);
        newVersion.setNetexId(train.getNetexId());
        // In a transaction, as the controllers and the import handler call the saver
        Train saved = transactionTemplate.execute(status -> (Train) vehicleTypeSaver.saveNewVersion(newVersion));

        // Then: the vehicle is of the new version, which is a train of the new size
        transactionTemplate.executeWithoutResult(status -> {
            Vehicle current = vehicleRepository.findFirstByNetexIdOrderByVersionDesc(vehicle.getNetexId());
            assertThat(current.getTransportType().getId()).isEqualTo(saved.getId()).isNotEqualTo(train.getId());
            assertThat(current.getTransportType()).isInstanceOf(Train.class);
            assertThat(((Train) current.getTransportType()).getNumberOfCars()).isEqualTo(3);
        });
    }

    private Train saveTrain(VehicleTypeKindEnumeration kind, String name, int cars) {
        return (Train) vehicleTypeSaver.saveNewVersion(trainWithoutId(kind, name, cars));
    }

    private static Train trainWithoutId(VehicleTypeKindEnumeration kind, String name, int cars) {
        Train train = new Train();
        train.setName(new EmbeddableMultilingualString(name, "en"));
        train.setTransportMode(AllPublicTransportModesEnumeration.RAIL);
        train.setDataOwnerRef(DATA_OWNER);
        train.setVehicleTypeKind(kind);
        train.setNumberOfCars(cars);
        TrainComponent component = new TrainComponent();
        component.setTrainElementRef("AKT:TrainElement:" + name.replace(' ', '-'));
        train.getComponents().add(component);
        return train;
    }

    private Vehicle saveVehicleOf(Train train) {
        Vehicle vehicle = new Vehicle();
        vehicle.setName(new EmbeddableMultilingualString("References vehicle", "en"));
        vehicle.setTransportType(train);
        vehicle.setDataOwnerRef(DATA_OWNER);
        return vehicleSaver.saveNewVersion(vehicle);
    }

    private VehicleModel saveModelOf(Train train) {
        VehicleModel model = new VehicleModel();
        model.setName(new EmbeddableMultilingualString("References model", "en"));
        model.setTransportType(train);
        return vehicleModelSaver.saveNewVersion(model);
    }

    private List<String> modelIds(VehicleTypeKindEnumeration kind) {
        return vehicleModelRepository.findCurrentFiltered(DATA_OWNER, null, null, kind, null, Pageable.unpaged())
                .getContent().stream().map(VehicleModel::getNetexId).toList();
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
                .extract().body().asString();
    }

    /** Matches the element itself, not references to it (those use ref="..."). */
    private static String idAttribute(String netexId) {
        return "id=\"" + netexId + "\"";
    }
}

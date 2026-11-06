package org.rutebanken.sobek.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rutebanken.sobek.model.vehicle.CompoundTrain;
import org.rutebanken.sobek.model.vehicle.ThroughAccessEnumeration;
import org.rutebanken.sobek.model.vehicle.Train;
import org.rutebanken.sobek.model.vehicle.TrainComponent;
import org.rutebanken.sobek.model.vehicle.TrainInCompoundTrain;
import org.rutebanken.sobek.model.vehicle.TrainSizeEnumeration;
import org.rutebanken.sobek.model.vehicle.VehicleOrientationEnumeration;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.model.vehicle.VehicleTypeKindEnumeration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import org.rutebanken.sobek.model.ValidBetween;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the V15 migration and the single-table mapping of Train and CompoundTrain.
 */
@SpringBootTest
public class TrainPersistenceTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private VehicleTypeRepository vehicleTypeRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    public void trainAndCompoundTrainRoundTripWithOrderedComponents() {
        long[] ids = transactionTemplate.execute(status -> {
            Train train = new Train();
            train.setNetexId("TST:Train:persist-1");
            train.setVersion(1);
            train.setVehicleTypeKind(VehicleTypeKindEnumeration.PHYSICAL);
            train.setNumberOfCars(3);
            train.setTrainSizeType(TrainSizeEnumeration.SHORT);
            train.setComponents(new ArrayList<>(List.of(
                    component("first", VehicleOrientationEnumeration.FORWARDS, "TST:TrainElement:1"),
                    component("second", VehicleOrientationEnumeration.BACKWARDS, "TST:TrainElement:2"),
                    component("third", VehicleOrientationEnumeration.UNKNOWN, "TST:TrainElement:3"))));
            entityManager.persist(train);

            CompoundTrain compound = new CompoundTrain();
            compound.setNetexId("CVT:CompoundTrain:persist-1");
            compound.setVersion(1);
            compound.setVehicleTypeKind(VehicleTypeKindEnumeration.CONTRACTUAL);
            TrainInCompoundTrain a = inCompound("a", train);
            TrainInCompoundTrain b = inCompound("b", train);
            compound.setComponents(new ArrayList<>(List.of(b, a)));
            entityManager.persist(compound);

            VehicleType plain = new VehicleType();
            plain.setNetexId("TST:VehicleType:persist-1");
            plain.setVersion(1);
            plain.setVehicleTypeKind(VehicleTypeKindEnumeration.PHYSICAL);
            entityManager.persist(plain);
            return new long[]{train.getId(), compound.getId(), plain.getId()};
        });

        transactionTemplate.executeWithoutResult(status -> {
            entityManager.clear();

            VehicleType loadedTrain = entityManager.find(VehicleType.class, ids[0]);
            assertThat(loadedTrain).isInstanceOf(Train.class);
            Train t = (Train) loadedTrain;
            assertThat(t.getNumberOfCars()).isEqualTo(3);
            assertThat(t.getTrainSizeType()).isEqualTo(TrainSizeEnumeration.SHORT);
            assertThat(t.getComponents()).extracting(c -> c.getLabel().getValue())
                    .containsExactly("first", "second", "third");
            assertThat(t.getComponents()).extracting(TrainComponent::getTrainElementRef)
                    .containsExactly("TST:TrainElement:1", "TST:TrainElement:2", "TST:TrainElement:3");
            assertThat(t.getComponents().get(1).getForwardCouplingThroughAccess()).isEqualTo(ThroughAccessEnumeration.DOOR);

            VehicleType loadedCompound = entityManager.find(VehicleType.class, ids[1]);
            assertThat(loadedCompound).isInstanceOf(CompoundTrain.class);
            CompoundTrain c = (CompoundTrain) loadedCompound;
            assertThat(c.getComponents()).extracting(x -> x.getLabel().getValue()).containsExactly("b", "a");
            assertThat(c.getComponents().get(0).getTrain().getId()).isEqualTo(ids[0]);

            VehicleType loadedPlain = entityManager.find(VehicleType.class, ids[2]);
            assertThat(loadedPlain).isNotInstanceOf(Train.class).isNotInstanceOf(CompoundTrain.class);
        });
    }

    /**
     * Components point at one specific version (database row) of the train they include. When that train gets a new
     * version, the components of trains that are in use now must follow it to the new version, or they would keep
     * pointing at a version that has been replaced. Components of old, expired versions are history and must keep
     * pointing at what was current when they were valid.
     *
     * <pre>
     * Train "Part" has two versions: partV1 and partV2 (the new one).
     * Currently valid owners of partV1:   "Express" (a Train) and "Fleet" (a CompoundTrain)
     * Expired owner of partV1:            "Retired" (a Train that was only valid in 2020)
     * </pre>
     */
    @Test
    @DisplayName("When a train gets a new version, only current owners' components move to it")
    public void componentReferencesMoveToTheNewVersionOnlyForCurrentOwners() {
        // Given
        Fixture fixture = transactionTemplate.execute(status -> {
            Train partV1 = train("TST:Train:part", 1, null);
            entityManager.persist(partV1);
            Train partV2 = train("TST:Train:part", 2, null);
            entityManager.persist(partV2);

            Train express = train("TST:Train:express", 1, null);
            express.setComponents(new ArrayList<>(List.of(trainComponentTo(partV1))));
            entityManager.persist(express);

            Train retired = train("TST:Train:retired", 1,
                    new ValidBetween(Instant.parse("2020-01-01T00:00:00Z"), Instant.parse("2021-01-01T00:00:00Z")));
            retired.setComponents(new ArrayList<>(List.of(trainComponentTo(partV1))));
            entityManager.persist(retired);

            CompoundTrain compound = new CompoundTrain();
            compound.setNetexId("TST:CompoundTrain:compound");
            compound.setVersion(1);
            compound.setVehicleTypeKind(VehicleTypeKindEnumeration.PHYSICAL);
            compound.setComponents(new ArrayList<>(List.of(inCompound("express", partV1))));
            entityManager.persist(compound);

            return new Fixture(partV1.getId(), partV2.getId(), express.getId(), retired.getId(), compound.getId());
        });

        // When partV1 is replaced by partV2 (new version)
        transactionTemplate.executeWithoutResult(status ->
                vehicleTypeRepository.moveComponentReferences(fixture.partV1, fixture.partV2));

        // Then
        transactionTemplate.executeWithoutResult(status -> {
            entityManager.clear();

            Train express = (Train) entityManager.find(VehicleType.class, fixture.express);
            assertThat(express.getComponents().get(0).getTrain().getId())
                    .as("current Train owner follows to the new version").isEqualTo(fixture.partV2);

            CompoundTrain compound = (CompoundTrain) entityManager.find(VehicleType.class, fixture.compound);
            assertThat(compound.getComponents().get(0).getTrain().getId())
                    .as("current CompoundTrain owner follows to the new version").isEqualTo(fixture.partV2);

            Train retired = (Train) entityManager.find(VehicleType.class, fixture.retired);
            assertThat(retired.getComponents().get(0).getTrain().getId())
                    .as("expired owner keeps pointing at the version that was current then").isEqualTo(fixture.partV1);
        });
    }

    private record Fixture(long partV1, long partV2, long express, long retired, long compound) {
    }

    private static Train train(String netexId, long version, ValidBetween validBetween) {
        Train train = new Train();
        train.setNetexId(netexId);
        train.setVersion(version);
        train.setVehicleTypeKind(VehicleTypeKindEnumeration.PHYSICAL);
        train.setValidBetween(validBetween);
        return train;
    }

    private static TrainComponent trainComponentTo(Train target) {
        TrainComponent component = new TrainComponent();
        component.setVersion(1);
        component.setTrain(target);
        return component;
    }

    private static TrainComponent component(String label, VehicleOrientationEnumeration orientation, String elementRef) {
        TrainComponent component = new TrainComponent();
        component.setVersion(1);
        component.setLabel(multilingual(label));
        component.setOperationalOrientation(orientation);
        component.setTrainElementRef(elementRef);
        component.setForwardCouplingThroughAccess(ThroughAccessEnumeration.DOOR);
        return component;
    }

    private static TrainInCompoundTrain inCompound(String label, Train train) {
        TrainInCompoundTrain component = new TrainInCompoundTrain();
        component.setVersion(1);
        component.setLabel(multilingual(label));
        component.setTrain(train);
        component.setOperationalOrientation(VehicleOrientationEnumeration.FORWARDS);
        return component;
    }

    private static org.rutebanken.sobek.model.EmbeddableMultilingualString multilingual(String value) {
        return new org.rutebanken.sobek.model.EmbeddableMultilingualString(value, "no");
    }
}

package org.rutebanken.sobek.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rutebanken.sobek.model.vehicle.CompoundTrain;
import org.rutebanken.sobek.model.vehicle.ThroughAccessEnumeration;
import org.rutebanken.sobek.model.vehicle.Train;
import org.rutebanken.sobek.model.vehicle.TrainComponent;
import org.rutebanken.sobek.model.vehicle.TrainElement;
import org.rutebanken.sobek.model.vehicle.TrainElementTypeEnumeration;
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
            TrainElement first = element("TST:TrainElement:1", 1, null);
            TrainElement second = element("TST:TrainElement:2", 1, null);
            TrainElement third = element("TST:TrainElement:3", 1, null);
            entityManager.persist(first);
            entityManager.persist(second);
            entityManager.persist(third);

            Train train = new Train();
            train.setNetexId("TST:Train:persist-1");
            train.setVersion(1);
            train.setVehicleTypeKind(VehicleTypeKindEnumeration.PHYSICAL);
            train.setNumberOfCars(3);
            train.setTrainSizeType(TrainSizeEnumeration.SHORT);
            train.setComponents(new ArrayList<>(List.of(
                    component("first", VehicleOrientationEnumeration.FORWARDS, first),
                    component("second", VehicleOrientationEnumeration.BACKWARDS, second),
                    component("third", VehicleOrientationEnumeration.UNKNOWN, third))));
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
            assertThat(t.getComponents()).extracting(x -> x.getTrainElement().getNetexId())
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
     * A compound train points at one specific version (database row) of each train in it. When such a train gets a new
     * version, the compound trains that are in use now must follow it to the new version, or they would keep pointing at
     * a version that has been replaced. Compound trains that have expired are history and must keep pointing at what was
     * current when they were valid.
     *
     * <pre>
     * Train "Part" has two versions: partV1 and partV2 (the new one).
     * Currently valid compound trains with Part:  "Fleet" and "Shuttle"
     * Expired compound train with Part:           "Retired" (only valid in 2020)
     * </pre>
     */
    @Test
    @DisplayName("When a train gets a new version, only current compound trains move to it")
    public void componentReferencesMoveToTheNewVersionOnlyForCurrentOwners() {
        // Given
        Fixture fixture = transactionTemplate.execute(status -> {
            Train partV1 = train("TST:Train:part", 1, null);
            entityManager.persist(partV1);
            Train partV2 = train("TST:Train:part", 2, null);
            entityManager.persist(partV2);

            CompoundTrain fleet = compoundTrain("TST:CompoundTrain:fleet", null, partV1);
            entityManager.persist(fleet);
            CompoundTrain shuttle = compoundTrain("TST:CompoundTrain:shuttle", null, partV1);
            entityManager.persist(shuttle);
            CompoundTrain retired = compoundTrain("TST:CompoundTrain:retired",
                    new ValidBetween(Instant.parse("2020-01-01T00:00:00Z"), Instant.parse("2021-01-01T00:00:00Z")), partV1);
            entityManager.persist(retired);

            return new Fixture(partV1.getId(), partV2.getId(), fleet.getId(), shuttle.getId(), retired.getId());
        });

        // When partV1 is replaced by partV2 (new version)
        transactionTemplate.executeWithoutResult(status ->
                vehicleTypeRepository.moveComponentReferences(fixture.partV1, fixture.partV2));

        // Then
        transactionTemplate.executeWithoutResult(status -> {
            entityManager.clear();

            assertThat(trainOf(fixture.fleet))
                    .as("current compound train follows to the new version").isEqualTo(fixture.partV2);
            assertThat(trainOf(fixture.shuttle))
                    .as("every current compound train follows to the new version").isEqualTo(fixture.partV2);
            assertThat(trainOf(fixture.retired))
                    .as("expired compound train keeps pointing at the version that was current then").isEqualTo(fixture.partV1);
        });
    }

    /**
     * A train component points at one specific version (database row) of its train element. As for the trains of a
     * compound train, the trains that are in use now follow a new version of the element, and the ones that have
     * expired keep pointing at what was current when they were valid.
     */
    @Test
    @DisplayName("When a train element gets a new version, only current trains move to it")
    public void trainComponentsMoveToTheNewVersionOfTheirElementOnlyForCurrentTrains() {
        // Given: an element with two versions, a train in use now and a train that expired in 2021
        long[] ids = transactionTemplate.execute(status -> {
            TrainElement elementV1 = element("TST:TrainElement:moving", 1, null);
            entityManager.persist(elementV1);
            TrainElement elementV2 = element("TST:TrainElement:moving", 2, null);
            entityManager.persist(elementV2);

            Train current = train("TST:Train:uses-element", 1, null);
            current.setComponents(new ArrayList<>(List.of(component("car", VehicleOrientationEnumeration.FORWARDS, elementV1))));
            entityManager.persist(current);
            Train expired = train("TST:Train:used-element", 1,
                    new ValidBetween(Instant.parse("2020-01-01T00:00:00Z"), Instant.parse("2021-01-01T00:00:00Z")));
            expired.setComponents(new ArrayList<>(List.of(component("car", VehicleOrientationEnumeration.FORWARDS, elementV1))));
            entityManager.persist(expired);
            return new long[]{elementV1.getId(), elementV2.getId(), current.getId(), expired.getId()};
        });

        // When
        transactionTemplate.executeWithoutResult(status -> vehicleTypeRepository.moveComponentReferences(ids[0], ids[1]));

        // Then
        transactionTemplate.executeWithoutResult(status -> {
            entityManager.clear();
            assertThat(elementOf(ids[2])).as("the current train follows to the new version").isEqualTo(ids[1]);
            assertThat(elementOf(ids[3])).as("the expired train keeps the version that was current then").isEqualTo(ids[0]);
        });
    }

    private long elementOf(long trainId) {
        Train train = (Train) entityManager.find(VehicleType.class, trainId);
        return train.getComponents().get(0).getTrainElement().getId();
    }

    private static TrainElement element(String netexId, long version, ValidBetween validBetween) {
        TrainElement element = new TrainElement();
        element.setNetexId(netexId);
        element.setVersion(version);
        element.setVehicleTypeKind(VehicleTypeKindEnumeration.PHYSICAL);
        element.setTrainElementType(TrainElementTypeEnumeration.CARRIAGE);
        element.setValidBetween(validBetween);
        return element;
    }

    private long trainOf(long compoundTrainId) {
        CompoundTrain compound = (CompoundTrain) entityManager.find(VehicleType.class, compoundTrainId);
        return compound.getComponents().get(0).getTrain().getId();
    }

    private record Fixture(long partV1, long partV2, long fleet, long shuttle, long retired) {
    }

    private static CompoundTrain compoundTrain(String netexId, ValidBetween validBetween, Train includedTrain) {
        CompoundTrain compound = new CompoundTrain();
        compound.setNetexId(netexId);
        compound.setVersion(1);
        compound.setVehicleTypeKind(VehicleTypeKindEnumeration.PHYSICAL);
        compound.setValidBetween(validBetween);
        compound.setComponents(new ArrayList<>(List.of(inCompound("part", includedTrain))));
        return compound;
    }

    private static Train train(String netexId, long version, ValidBetween validBetween) {
        Train train = new Train();
        train.setNetexId(netexId);
        train.setVersion(version);
        train.setVehicleTypeKind(VehicleTypeKindEnumeration.PHYSICAL);
        train.setValidBetween(validBetween);
        return train;
    }

    private static TrainComponent component(String label, VehicleOrientationEnumeration orientation, TrainElement element) {
        TrainComponent component = new TrainComponent();
        component.setVersion(1);
        component.setLabel(multilingual(label));
        component.setOperationalOrientation(orientation);
        component.setTrainElement(element);
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

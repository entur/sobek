package org.rutebanken.sobek.repository;

import jakarta.persistence.EntityManager;
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

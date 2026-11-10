package org.rutebanken.sobek.versioning;

import org.junit.jupiter.api.Test;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;
import org.rutebanken.sobek.model.vehicle.CompoundTrain;
import org.rutebanken.sobek.model.vehicle.ThroughAccessEnumeration;
import org.rutebanken.sobek.model.vehicle.Train;
import org.rutebanken.sobek.model.vehicle.TrainComponent;
import org.rutebanken.sobek.model.vehicle.TrainElement;
import org.rutebanken.sobek.model.vehicle.TrainInCompoundTrain;
import org.rutebanken.sobek.model.vehicle.TrainSizeEnumeration;
import org.rutebanken.sobek.model.vehicle.Vehicle;
import org.rutebanken.sobek.model.vehicle.VehicleOrientationEnumeration;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.model.vehicle.VehicleTypeKindEnumeration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A new version of a Train or CompoundTrain must stay a Train or CompoundTrain and keep its ordered components.
 */
@SpringBootTest
public class TrainVersionCopyTest {

    @Autowired
    private VersionCreator versionCreator;

    @Test
    public void copyOfTrainIsATrainWithCopiedComponentsInOrder() {
        Train original = train("TST:Train:1", 1L);
        original.setComponents(new ArrayList<>(List.of(trainComponent("first", "TST:TrainElement:1", 10L),
                trainComponent("second", "TST:TrainElement:2", 11L))));

        VehicleType copy = versionCreator.createCopy((VehicleType) original, VehicleType.class);

        assertThat(copy).isInstanceOf(Train.class);
        Train trainCopy = (Train) copy;
        assertThat(trainCopy).isNotSameAs(original);
        assertThat(trainCopy.getId()).isNull();
        assertThat(trainCopy.getNetexId()).isEqualTo("TST:Train:1");
        assertThat(trainCopy.getVehicleTypeKind()).isEqualTo(VehicleTypeKindEnumeration.PHYSICAL);
        assertThat(trainCopy.getNumberOfCars()).isEqualTo(4);
        assertThat(trainCopy.getTrainSizeType()).isEqualTo(TrainSizeEnumeration.LONG);
        assertThat(trainCopy.getComponents()).extracting(c -> c.getLabel().getValue()).containsExactly("first", "second");
        // A copied component places the same train element as the original: only the train is versioned
        assertThat(trainCopy.getComponents()).extracting(c -> c.getTrainElement().getNetexId())
                .containsExactly("TST:TrainElement:1", "TST:TrainElement:2");
        assertThat(trainCopy.getComponents()).extracting(TrainComponent::getTrainElement)
                .containsExactly(original.getComponents().get(0).getTrainElement(), original.getComponents().get(1).getTrainElement());
        assertThat(trainCopy.getComponents()).extracting(TrainComponent::getForwardCouplingThroughAccess)
                .containsOnly(ThroughAccessEnumeration.DOOR);
        // The components are new rows, not the same objects as the original's
        assertThat(trainCopy.getComponents()).extracting(TrainComponent::getId).containsOnlyNulls();
        assertThat(trainCopy.getComponents().get(0)).isNotSameAs(original.getComponents().get(0));
    }

    @Test
    public void copyOfCompoundTrainIsACompoundTrainWithComponentsPointingAtTheSameTrains() {
        Train a = train("TST:Train:1", 1L);
        a.setId(1L);
        CompoundTrain original = new CompoundTrain();
        original.setId(5L);
        original.setNetexId("CVT:CompoundTrain:1");
        original.setVersion(2L);
        original.setVehicleTypeKind(VehicleTypeKindEnumeration.CONTRACTUAL);
        original.setComponents(new ArrayList<>(List.of(inCompound("b", a), inCompound("a", a))));

        VehicleType copy = versionCreator.createCopy((VehicleType) original, VehicleType.class);

        assertThat(copy).isInstanceOf(CompoundTrain.class);
        CompoundTrain compoundCopy = (CompoundTrain) copy;
        assertThat(compoundCopy.getId()).isNull();
        assertThat(compoundCopy.getVehicleTypeKind()).isEqualTo(VehicleTypeKindEnumeration.CONTRACTUAL);
        assertThat(compoundCopy.getComponents()).extracting(c -> c.getLabel().getValue()).containsExactly("b", "a");
        assertThat(compoundCopy.getComponents()).extracting(TrainInCompoundTrain::getId).containsOnlyNulls();
        // The referenced train is the same row, not a copy of it
        assertThat(compoundCopy.getComponents()).allSatisfy(c -> assertThat(c.getTrain()).isSameAs(a));
    }

    @Test
    public void copyOfPlainVehicleTypeStaysAPlainVehicleType() {
        VehicleType original = new VehicleType();
        original.setId(3L);
        original.setNetexId("TST:VehicleType:1");
        original.setVersion(1L);
        original.setVehicleTypeKind(VehicleTypeKindEnumeration.PHYSICAL);

        VehicleType copy = versionCreator.createCopy(original, VehicleType.class);

        assertThat(copy).isNotInstanceOf(Train.class).isNotInstanceOf(CompoundTrain.class);
        assertThat(copy.getId()).isNull();
    }

    @Test
    public void copyOfVehicleKeepsReferringToTheSameTrain() {
        Train transportType = train("TST:Train:1", 1L);
        transportType.setId(9L);
        Vehicle original = new Vehicle();
        original.setId(1L);
        original.setVersion(1L);
        original.setTransportType(transportType);

        Vehicle copy = versionCreator.createCopy(original, Vehicle.class);

        assertThat(copy.getId()).isNull();
        assertThat(copy.getTransportType()).isSameAs(transportType);
    }

    private static Train train(String netexId, long version) {
        Train train = new Train();
        train.setId(1L);
        train.setNetexId(netexId);
        train.setVersion(version);
        train.setVehicleTypeKind(VehicleTypeKindEnumeration.PHYSICAL);
        train.setNumberOfCars(4);
        train.setTrainSizeType(TrainSizeEnumeration.LONG);
        return train;
    }

    private static TrainComponent trainComponent(String label, String elementRef, Long id) {
        TrainComponent component = new TrainComponent();
        component.setId(id);
        component.setVersion(1L);
        component.setLabel(new EmbeddableMultilingualString(label, "no"));
        TrainElement element = new TrainElement();
        element.setId(100L + id);
        element.setNetexId(elementRef);
        element.setVersion(1L);
        element.setVehicleTypeKind(VehicleTypeKindEnumeration.PHYSICAL);
        component.setTrainElement(element);
        component.setOperationalOrientation(VehicleOrientationEnumeration.FORWARDS);
        component.setForwardCouplingThroughAccess(ThroughAccessEnumeration.DOOR);
        return component;
    }

    private static TrainInCompoundTrain inCompound(String label, Train target) {
        TrainInCompoundTrain component = new TrainInCompoundTrain();
        component.setId(30L);
        component.setVersion(1L);
        component.setLabel(new EmbeddableMultilingualString(label, "no"));
        component.setTrain(target);
        component.setOperationalOrientation(VehicleOrientationEnumeration.BACKWARDS);
        return component;
    }
}

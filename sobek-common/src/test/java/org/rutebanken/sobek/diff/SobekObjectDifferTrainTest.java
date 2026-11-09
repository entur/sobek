package org.rutebanken.sobek.diff;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rutebanken.sobek.diff.generic.DiffType;
import org.rutebanken.sobek.diff.generic.Difference;
import org.rutebanken.sobek.diff.generic.GenericObjectDiffer;
import org.rutebanken.sobek.model.EmbeddableMultilingualString;
import org.rutebanken.sobek.model.vehicle.Train;
import org.rutebanken.sobek.model.vehicle.TrainComponent;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * How the differ reports changes to the components of a train. The order of the components is part of what a train is,
 * so a component is identified by its position and not by its id.
 */
class SobekObjectDifferTrainTest {

    private final SobekObjectDiffer differ = new SobekObjectDiffer(new GenericObjectDiffer());

    @Test
    @DisplayName("Two trains with the same components give no differences, whatever the instances")
    void sameComponentsGiveNoDifferences() throws IllegalAccessException {
        Train before = train(component("a", "front"), component("b", "rear"));
        Train after = train(component("a", "front"), component("b", "rear"));

        assertThat(differ.compareObjects(before, after)).isEmpty();
    }

    @Test
    @DisplayName("Swapping two components is a change in both positions")
    void swappingComponentsIsReported() throws IllegalAccessException {
        Train before = train(component("a", "front"), component("b", "rear"));
        Train after = train(component("b", "rear"), component("a", "front"));

        List<Difference> differences = differ.compareObjects(before, after);

        assertThat(differences).extracting(d -> d.property)
                .anyMatch(property -> property.contains("components[0]"))
                .anyMatch(property -> property.contains("components[1]"));
    }

    @Test
    @DisplayName("A change inside a component is reported with the position of the component")
    void changeInsideComponentIsReportedWithItsPosition() throws IllegalAccessException {
        Train before = train(component("a", "front"), component("b", "rear"));
        Train after = train(component("a", "front"), component("b", "rear car"));

        List<Difference> differences = differ.compareObjects(before, after);

        assertThat(differences).hasSize(1);
        assertThat(differences.getFirst().property).contains("components[1]").contains("label");
        assertThat(differences.getFirst().toString()).contains("rear").contains("rear car");
    }

    @Test
    @DisplayName("An added or removed component is reported with its position")
    void addedAndRemovedComponentsAreReportedWithTheirPosition() throws IllegalAccessException {
        Train two = train(component("a", "front"), component("b", "rear"));
        Train three = train(component("a", "front"), component("b", "rear"), component("c", "extra"));

        List<Difference> added = differ.compareObjects(two, three);
        List<Difference> removed = differ.compareObjects(three, two);

        assertThat(added).singleElement().satisfies(d -> {
            assertThat(d.diffType).isEqualTo(DiffType.COLLECTION_ADD);
            assertThat(d.property).contains("components[2]");
        });
        assertThat(removed).singleElement().satisfies(d -> {
            assertThat(d.diffType).isEqualTo(DiffType.COLLECTION_REMOVE);
            assertThat(d.property).contains("components[2]");
        });
    }

    @Test
    @DisplayName("Every changed component of a long train is reported, however many there are")
    void changesInAllComponentsOfALongTrainAreReported() throws IllegalAccessException {
        int cars = 15;
        Train before = train(IntStream.range(0, cars).mapToObj(i -> component("id" + i, "car " + i)).toArray(TrainComponent[]::new));
        Train after = train(IntStream.range(0, cars).mapToObj(i -> component("id" + i, "changed car " + i)).toArray(TrainComponent[]::new));

        assertThat(differ.compareObjects(before, after)).hasSize(cars);
    }

    private static Train train(TrainComponent... components) {
        Train train = new Train();
        train.setNetexId("AKT:Train:1");
        train.getComponents().addAll(List.of(components));
        return train;
    }

    private static TrainComponent component(String id, String label) {
        TrainComponent component = new TrainComponent();
        component.setNetexId("AKT:TrainComponent:" + id);
        component.setLabel(new EmbeddableMultilingualString(label, "en"));
        component.setTrainElementRef("AKT:TrainElement:" + id);
        return component;
    }
}

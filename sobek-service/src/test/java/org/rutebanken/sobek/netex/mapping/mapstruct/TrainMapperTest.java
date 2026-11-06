package org.rutebanken.sobek.netex.mapping.mapstruct;

import org.entur.netex2.model.CompoundTrain;
import org.entur.netex2.model.MultilingualString;
import org.entur.netex2.model.Train;
import org.entur.netex2.model.TrainComponent;
import org.entur.netex2.model.TrainComponentCouplingStructure;
import org.entur.netex2.model.TrainComponents_RelStructure;
import org.entur.netex2.model.TrainElementTypeRefStructure;
import org.entur.netex2.model.TrainInCompoundTrain_VersionedChildStructure;
import org.entur.netex2.model.TrainSizeStructure;
import org.entur.netex2.model.TrainsInCompoundTrain_RelStructure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.model.vehicle.VehicleTypeKindEnumeration;
import org.rutebanken.sobek.netex.mapping.NetexMappingException;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Mapping of NeTEx Train and CompoundTrain. References from a compound train to its trains are resolved against the
 * database, so they are covered by the import/export integration test; this test covers the rest.
 */
@SpringBootTest
class TrainMapperTest {

    @Autowired
    private VehicleTypeMapper mapper;
    @Autowired
    private MappingContext context;
    @Autowired
    private KeyListStructureMapper keyListStructureMapper;

    @BeforeEach
    void setUp() {
        context.setKeyListStructureMapper(keyListStructureMapper);
    }

    @Test
    @DisplayName("A NeTEx Train becomes a Sobek Train with its size and its components in the order given")
    void trainToSobek() {
        // The components are given out of order: the order attribute decides
        Train netex = new Train().withId("AKT:Train:1").withVersion("1")
                .withTrainSize(new TrainSizeStructure().withNumberOfCars(BigInteger.valueOf(4))
                        .withTrainSizeType(org.entur.netex2.model.TrainSizeEnumeration.LONG))
                .withComponents(new TrainComponents_RelStructure().withTrainComponentRefOrTrainComponent(
                        component("second", 2, "AKT:TrainElement:b"),
                        component("first", 1, "AKT:TrainElement:a")));

        VehicleType mapped = mapper.mapAnyToSobek(netex, context);

        assertThat(mapped).isInstanceOf(org.rutebanken.sobek.model.vehicle.Train.class);
        var train = (org.rutebanken.sobek.model.vehicle.Train) mapped;
        assertThat(train.getNumberOfCars()).isEqualTo(4);
        assertThat(train.getTrainSizeType()).isEqualTo(org.rutebanken.sobek.model.vehicle.TrainSizeEnumeration.LONG);
        assertThat(train.getComponents()).extracting(c -> c.getLabel().getValue()).containsExactly("first", "second");
        assertThat(train.getComponents()).extracting(org.rutebanken.sobek.model.vehicle.TrainComponent::getTrainElementRef)
                .containsExactly("AKT:TrainElement:a", "AKT:TrainElement:b");
        assertThat(train.getComponents().getFirst().getForwardCouplingThroughAccess())
                .isEqualTo(org.rutebanken.sobek.model.vehicle.ThroughAccessEnumeration.DOOR);
        assertThat(train.getComponents().getFirst().getOperationalOrientation())
                .isEqualTo(org.rutebanken.sobek.model.vehicle.VehicleOrientationEnumeration.BACKWARDS);
    }

    @Test
    @DisplayName("A component keeps an id in a registered codespace; any other id, or none, is left for the saver to assign")
    void componentIdsFollowTheCodespaceRules() {
        TrainComponent registered = component("registered", 1, "AKT:TrainElement:a");
        registered.setId("AKT:TrainComponent:1");
        TrainComponent external = component("external", 2, "AKT:TrainElement:b");
        external.setId("OTHER:TrainComponent:2");
        TrainComponent withoutId = component("none", 3, "AKT:TrainElement:c");
        Train netex = new Train().withId("AKT:Train:1")
                .withComponents(new TrainComponents_RelStructure().withTrainComponentRefOrTrainComponent(registered, external, withoutId));

        var train = (org.rutebanken.sobek.model.vehicle.Train) mapper.mapAnyToSobek(netex, context);

        assertThat(train.getComponents()).extracting(org.rutebanken.sobek.model.vehicle.TrainComponent::getNetexId)
                .containsExactly("AKT:TrainComponent:1", null, null);
    }

    @Test
    @DisplayName("Components without an order keep their place in the file")
    void componentsWithoutOrderKeepFilePosition() {
        Train netex = new Train().withId("AKT:Train:1")
                .withComponents(new TrainComponents_RelStructure().withTrainComponentRefOrTrainComponent(
                        component("a", null, "AKT:TrainElement:a"),
                        component("b", null, "AKT:TrainElement:b")));

        var train = (org.rutebanken.sobek.model.vehicle.Train) mapper.mapAnyToSobek(netex, context);

        assertThat(train.getComponents()).extracting(c -> c.getLabel().getValue()).containsExactly("a", "b");
    }

    @Test
    @DisplayName("A Sobek Train is exported with its size and its components numbered in list order")
    void trainToNetex() {
        var train = new org.rutebanken.sobek.model.vehicle.Train();
        train.setNetexId("AKT:Train:1");
        train.setVersion(3);
        train.setVehicleTypeKind(VehicleTypeKindEnumeration.PHYSICAL);
        train.setNumberOfCars(2);
        train.setTrainSizeType(org.rutebanken.sobek.model.vehicle.TrainSizeEnumeration.SHORT);
        train.getComponents().add(sobekComponent("b", "AKT:TrainElement:b"));
        train.getComponents().add(sobekComponent("a", "AKT:TrainElement:a"));

        var netex = mapper.mapAnyToNetex(train, context);

        assertThat(netex).isInstanceOf(Train.class);
        var netexTrain = (Train) netex;
        assertThat(netexTrain.getId()).isEqualTo("AKT:Train:1");
        assertThat(netexTrain.getVersion()).isEqualTo("3");
        assertThat(netexTrain.getTrainSize().getNumberOfCars()).isEqualTo(BigInteger.TWO);
        var components = netexTrain.getComponents().getTrainComponentRefOrTrainComponent().stream().map(TrainComponent.class::cast).toList();
        assertThat(components).extracting(TrainComponent::getOrder).containsExactly(BigInteger.ONE, BigInteger.TWO);
        assertThat(components).extracting(c -> c.getTrainElementRef().getRef()).containsExactly("AKT:TrainElement:b", "AKT:TrainElement:a");
    }

    @Test
    @DisplayName("A Train without components or size has no empty containers in NeTEx")
    void emptyTrainHasNoEmptyContainers() {
        var train = new org.rutebanken.sobek.model.vehicle.Train();
        train.setNetexId("AKT:Train:1");
        train.setVehicleTypeKind(VehicleTypeKindEnumeration.PHYSICAL);

        var netex = (Train) mapper.mapAnyToNetex(train, context);

        assertThat(netex.getTrainSize()).isNull();
        assertThat(netex.getComponents()).isNull();
    }

    @Test
    @DisplayName("A CompoundTrain without components maps to a CompoundTrain")
    void compoundTrainKeepsItsType() {
        var compound = new org.rutebanken.sobek.model.vehicle.CompoundTrain();
        compound.setNetexId("CVT:CompoundTrain:1");
        compound.setVehicleTypeKind(VehicleTypeKindEnumeration.CONTRACTUAL);

        assertThat(mapper.mapAnyToNetex(compound, context)).isInstanceOf(CompoundTrain.class);
        assertThat(mapper.mapAnyToSobek(new CompoundTrain().withId("CVT:CompoundTrain:1"), context))
                .isInstanceOf(org.rutebanken.sobek.model.vehicle.CompoundTrain.class);
    }

    @Test
    @DisplayName("The kind of a train follows the codespace of its id, as for any vehicle type")
    void kindFollowsCodespace() {
        assertThat(mapper.mapAnyToSobek(new Train().withId("CVT:Train:1"), context).getVehicleTypeKind())
                .isEqualTo(VehicleTypeKindEnumeration.CONTRACTUAL);
        assertThat(mapper.mapAnyToSobek(new Train().withId("AKT:Train:1"), context).getVehicleTypeKind())
                .isEqualTo(VehicleTypeKindEnumeration.PHYSICAL);
    }

    @Test
    @DisplayName("A component given by reference, or without a train element, is rejected")
    void unsupportedComponentsAreRejected() {
        Train byReference = new Train().withId("AKT:Train:1")
                .withComponents(new TrainComponents_RelStructure().withTrainComponentRefOrTrainComponent(
                        new org.entur.netex2.model.TrainComponentRefStructure().withRef("AKT:TrainComponent:1")));
        Train withoutElement = new Train().withId("AKT:Train:2")
                .withComponents(new TrainComponents_RelStructure().withTrainComponentRefOrTrainComponent(new TrainComponent()));

        assertThatThrownBy(() -> mapper.mapAnyToSobek(byReference, context)).isInstanceOf(NetexMappingException.class);
        assertThatThrownBy(() -> mapper.mapAnyToSobek(withoutElement, context)).isInstanceOf(NetexMappingException.class)
                .hasMessageContaining("TrainElementRef");
    }

    @Test
    @DisplayName("A compound train component that is not a reference to a train is rejected")
    void compoundTrainComponentWithoutTrainRefIsRejected() {
        CompoundTrain netex = new CompoundTrain().withId("AKT:CompoundTrain:1")
                .withComponents(new TrainsInCompoundTrain_RelStructure()
                        .withTrainInCompoundTrain(new TrainInCompoundTrain_VersionedChildStructure()));

        assertThatThrownBy(() -> mapper.mapAnyToSobek(netex, context)).isInstanceOf(NetexMappingException.class)
                .hasMessageContaining("TrainRef");
    }

    private static TrainComponent component(String label, Integer order, String elementRef) {
        TrainComponent component = new TrainComponent()
                .withLabel(new MultilingualString().withContent(label))
                .withTrainElementRef(new TrainElementTypeRefStructure().withRef(elementRef))
                .withOperationalOrientation(org.entur.netex2.model.VehicleOrientationEnumeration.BACKWARDS)
                .withForwardCoupling(new TrainComponentCouplingStructure().withThroughAccess(org.entur.netex2.model.ThroughAccessEnumeration.DOOR));
        component.setOrder(order == null ? null : BigInteger.valueOf(order));
        return component;
    }

    private static org.rutebanken.sobek.model.vehicle.TrainComponent sobekComponent(String label, String elementRef) {
        var component = new org.rutebanken.sobek.model.vehicle.TrainComponent();
        component.setLabel(new org.rutebanken.sobek.model.EmbeddableMultilingualString(label, "no"));
        component.setTrainElementRef(elementRef);
        component.setVersion(1);
        return component;
    }
}

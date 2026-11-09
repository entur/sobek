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
import org.rutebanken.sobek.repository.VehicleTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Mapping of NeTEx Train, CompoundTrain and TrainElement. The references of a compound train to its trains are resolved
 * against the database, so they are covered by the import/export integration test. The references of a train to its
 * train elements are covered here: the elements the tests refer to are saved first.
 */
@SpringBootTest
class TrainMapperTest {

    @Autowired
    private VehicleTypeMapper mapper;
    @Autowired
    private MappingContext context;
    @Autowired
    private KeyListStructureMapper keyListStructureMapper;
    @Autowired
    private VehicleTypeRepository vehicleTypeRepository;

    @BeforeEach
    void setUp() {
        context.setKeyListStructureMapper(keyListStructureMapper);
        saveIfMissing(new org.rutebanken.sobek.model.vehicle.TrainElement(), "AKT:TrainElement:a");
        saveIfMissing(new org.rutebanken.sobek.model.vehicle.TrainElement(), "AKT:TrainElement:b");
        saveIfMissing(new org.rutebanken.sobek.model.vehicle.TrainElement(), "AKT:TrainElement:c");
        saveIfMissing(new org.rutebanken.sobek.model.vehicle.Train(), "AKT:Train:not-an-element");
    }

    /** The database is shared by the tests of the application context, so the objects are saved once. */
    private void saveIfMissing(VehicleType vehicleType, String netexId) {
        if (vehicleTypeRepository.findFirstByNetexIdOrderByVersionDesc(netexId) == null) {
            vehicleType.setNetexId(netexId);
            vehicleType.setVersion(1);
            vehicleType.setDataOwnerRef("AKT");
            vehicleType.setVehicleTypeKind(VehicleTypeKindEnumeration.PHYSICAL);
            vehicleTypeRepository.save(vehicleType);
        }
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
        assertThat(train.getComponents()).extracting(c -> c.getTrainElement().getNetexId())
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
    @DisplayName("A component whose train element is not in Sobek, or is not a train element, is rejected")
    void componentWithUnknownOrWrongElementIsRejected() {
        Train missing = new Train().withId("AKT:Train:1")
                .withComponents(new TrainComponents_RelStructure().withTrainComponentRefOrTrainComponent(component("x", 1, "AKT:TrainElement:missing")));
        Train wrongKind = new Train().withId("AKT:Train:2")
                .withComponents(new TrainComponents_RelStructure().withTrainComponentRefOrTrainComponent(component("y", 1, "AKT:Train:not-an-element")));

        assertThatThrownBy(() -> mapper.mapAnyToSobek(missing, context)).isInstanceOf(NetexMappingException.class)
                .hasMessageContaining("AKT:TrainElement:missing").hasMessageContaining("not found");
        assertThatThrownBy(() -> mapper.mapAnyToSobek(wrongKind, context)).isInstanceOf(NetexMappingException.class)
                .hasMessageContaining("AKT:Train:not-an-element").hasMessageContaining("is not a train element");
    }

    @Test
    @DisplayName("A NeTEx TrainElement becomes a Sobek TrainElement with the fields of a vehicle type and its element type")
    void trainElementToSobek() {
        org.entur.netex2.model.TrainElement netex = new org.entur.netex2.model.TrainElement().withId("AKT:TrainElement:car").withVersion("1")
                .withName(new MultilingualString().withContent("Car").withLang("en"))
                .withTrainElementType(org.entur.netex2.model.TrainElementTypeTypeEnumeration.SLEEPER_CARRIAGE)
                .withLength(new java.math.BigDecimal("26.4"))
                .withBoardingHeight(new java.math.BigDecimal("0.55"));
        netex.setLowFloor(true);

        VehicleType mapped = mapper.mapToSobek(netex, context);

        assertThat(mapped).isInstanceOf(org.rutebanken.sobek.model.vehicle.TrainElement.class);
        var element = (org.rutebanken.sobek.model.vehicle.TrainElement) mapped;
        assertThat(element.getNetexId()).isEqualTo("AKT:TrainElement:car");
        assertThat(element.getName().getValue()).isEqualTo("Car");
        assertThat(element.getTrainElementType()).isEqualTo(org.rutebanken.sobek.model.vehicle.TrainElementTypeEnumeration.SLEEPER_CARRIAGE);
        assertThat(element.getLength()).isEqualByComparingTo("26.4");
        assertThat(element.getBoardingHeight()).isEqualByComparingTo("0.55");
        assertThat(element.getLowFloor()).isTrue();
        assertThat(element.getVehicleTypeKind()).isEqualTo(VehicleTypeKindEnumeration.PHYSICAL);
    }

    @Test
    @DisplayName("A Sobek TrainElement is exported as a TrainElement with its element type")
    void trainElementToNetex() {
        var element = new org.rutebanken.sobek.model.vehicle.TrainElement();
        element.setNetexId("CVT:TrainElement:7");
        element.setVersion(2);
        element.setVehicleTypeKind(VehicleTypeKindEnumeration.CONTRACTUAL);
        element.setTrainElementType(org.rutebanken.sobek.model.vehicle.TrainElementTypeEnumeration.ENGINE);
        element.setLength(new java.math.BigDecimal("20"));

        var netex = mapper.mapToNetex(element, context);

        assertThat(netex).isInstanceOf(org.entur.netex2.model.TrainElement.class);
        var netexElement = (org.entur.netex2.model.TrainElement) netex;
        assertThat(netexElement.getId()).isEqualTo("CVT:TrainElement:7");
        assertThat(netexElement.getVersion()).isEqualTo("2");
        assertThat(netexElement.getTrainElementType()).isEqualTo(org.entur.netex2.model.TrainElementTypeTypeEnumeration.ENGINE);
        assertThat(netexElement.getLength()).isEqualByComparingTo("20");
    }

    @Test
    @DisplayName("A train element is not a NeTEx vehicle type, so the vehicle type dispatcher rejects it")
    void trainElementIsNotMappedAsAVehicleType() {
        var element = new org.rutebanken.sobek.model.vehicle.TrainElement();
        element.setNetexId("AKT:TrainElement:1");

        assertThatThrownBy(() -> mapper.mapAnyToNetex(element, context)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("train element");
    }

    @Test
    @DisplayName("The kind of a train element follows the codespace of its id")
    void trainElementKindFollowsCodespace() {
        assertThat(mapper.mapToSobek(new org.entur.netex2.model.TrainElement().withId("CVT:TrainElement:1"), context).getVehicleTypeKind())
                .isEqualTo(VehicleTypeKindEnumeration.CONTRACTUAL);
        assertThat(mapper.mapToSobek(new org.entur.netex2.model.TrainElement().withId("AKT:TrainElement:1"), context).getVehicleTypeKind())
                .isEqualTo(VehicleTypeKindEnumeration.PHYSICAL);
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

    private static org.rutebanken.sobek.model.vehicle.TrainComponent sobekComponent(String label, String elementId) {
        var component = new org.rutebanken.sobek.model.vehicle.TrainComponent();
        component.setLabel(new org.rutebanken.sobek.model.EmbeddableMultilingualString(label, "no"));
        var element = new org.rutebanken.sobek.model.vehicle.TrainElement();
        element.setNetexId(elementId);
        component.setTrainElement(element);
        component.setVersion(1);
        return component;
    }
}

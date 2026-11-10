package org.rutebanken.sobek.versioning.save;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rutebanken.sobek.error.VehicleTypeKindViolationException;
import org.rutebanken.sobek.model.vehicle.CompoundTrain;
import org.rutebanken.sobek.model.vehicle.Train;
import org.rutebanken.sobek.model.vehicle.TrainComponent;
import org.rutebanken.sobek.model.vehicle.TrainElement;
import org.rutebanken.sobek.model.vehicle.TrainInCompoundTrain;
import org.rutebanken.sobek.model.vehicle.VehicleTypeKindEnumeration;
import org.rutebanken.sobek.netex.id.ValidPrefixList;
import org.rutebanken.sobek.repository.VehicleRepository;
import org.rutebanken.sobek.repository.VehicleTypeRepository;
import org.rutebanken.sobek.repository.listener.NetexIdAssigner;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * What the saver requires of the components of a Train and a CompoundTrain, before anything is saved.
 */
@ExtendWith(MockitoExtension.class)
class TrainComponentRulesTest {

    private static final String OWNER = "AKT";

    @Mock
    private VehicleTypeRepository vehicleTypeRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private DefaultMergingVersionedSaverService defaultVersionedSaverService;
    @Mock
    private NetexIdAssigner netexIdAssigner;

    private VehicleTypeVersionedSaverService saver;

    @BeforeEach
    void setUp() {
        saver = new VehicleTypeVersionedSaverService(vehicleTypeRepository, vehicleRepository, defaultVersionedSaverService, netexIdAssigner,
                new ValidPrefixList("NMR", List.of("AKT", "RUT")));
    }

    @Test
    @DisplayName("A train component without a train element, or with one that is not saved, is rejected")
    void trainComponentWithoutExistingElementIsRejected() {
        Train withoutElement = train("AKT:Train:1", OWNER);
        withoutElement.getComponents().add(component(savedElement("AKT:TrainElement:a", OWNER)));
        withoutElement.getComponents().add(component(null));
        Train withUnsavedElement = train("AKT:Train:2", OWNER);
        withUnsavedElement.getComponents().add(component(element("AKT:TrainElement:b", OWNER)));

        ValidationException e = assertThrows(ValidationException.class, () -> saver.saveNewVersion(null, withoutElement, Instant.now()));

        assertThat(e.getMessage()).contains("Component 2").contains("AKT:Train:1").contains("train element");
        assertThrows(ValidationException.class, () -> saver.saveNewVersion(null, withUnsavedElement, Instant.now()));
        verify(defaultVersionedSaverService, never()).saveNewVersion(any(), any(), any(Instant.class), any());
    }

    @Test
    @DisplayName("A train made of train elements of another data owner is rejected")
    void trainWithElementOfAnotherOwnerIsRejected() {
        Train train = train("AKT:Train:1", OWNER);
        train.getComponents().add(component(savedElement("AKT:TrainElement:a", OWNER)));
        train.getComponents().add(component(savedElement("RUT:TrainElement:9", "RUT")));

        ValidationException e = assertThrows(ValidationException.class, () -> saver.saveNewVersion(null, train, Instant.now()));

        assertThat(e.getMessage()).contains("Component 2").contains("RUT:TrainElement:9").contains("RUT").contains(OWNER);
        verify(defaultVersionedSaverService, never()).saveNewVersion(any(), any(), any(Instant.class), any());
    }

    @Test
    @DisplayName("A train and a compound train can only be made of elements and trains of their own kind")
    void partsOfAnotherKindAreRejected() {
        TrainElement contractualElement = savedElement("CVT:TrainElement:1", OWNER);
        contractualElement.setVehicleTypeKind(VehicleTypeKindEnumeration.CONTRACTUAL);
        Train physicalTrain = train("AKT:Train:1", OWNER);
        physicalTrain.getComponents().add(component(contractualElement));

        Train contractualTrain = savedTrain("CVT:Train:2", OWNER);
        contractualTrain.setVehicleTypeKind(VehicleTypeKindEnumeration.CONTRACTUAL);
        CompoundTrain physicalCompound = compoundTrain(OWNER, contractualTrain);

        assertThrows(VehicleTypeKindViolationException.class, () -> saver.saveNewVersion(null, physicalTrain, Instant.now()));
        assertThrows(VehicleTypeKindViolationException.class, () -> saver.saveNewVersion(null, physicalCompound, Instant.now()));
        verify(defaultVersionedSaverService, never()).saveNewVersion(any(), any(), any(Instant.class), any());
    }

    @Test
    @DisplayName("A train of train elements of its own data owner is saved")
    void trainOfElementsOfTheSameOwnerIsSaved() {
        when(defaultVersionedSaverService.saveNewVersion(any(), any(), any(Instant.class), any())).thenAnswer(inv -> inv.getArgument(1));
        Train train = train("AKT:Train:1", OWNER);
        train.getComponents().add(component(savedElement("AKT:TrainElement:a", OWNER)));

        saver.saveNewVersion(null, train, Instant.now());

        verify(defaultVersionedSaverService).saveNewVersion(any(), any(), any(Instant.class), any());
    }

    @Test
    @DisplayName("A compound train of trains of its own data owner is saved")
    void compoundTrainOfTrainsOfTheSameOwnerIsSaved() {
        when(defaultVersionedSaverService.saveNewVersion(any(), any(), any(Instant.class), any())).thenAnswer(inv -> inv.getArgument(1));
        CompoundTrain compound = compoundTrain(OWNER, savedTrain("AKT:Train:1", OWNER), savedTrain("AKT:Train:2", OWNER));

        saver.saveNewVersion(null, compound, Instant.now());

        verify(defaultVersionedSaverService).saveNewVersion(any(), any(), any(Instant.class), any());
    }

    @Test
    @DisplayName("A compound train that holds a train of another data owner is rejected")
    void compoundTrainWithTrainOfAnotherOwnerIsRejected() {
        CompoundTrain compound = compoundTrain(OWNER, savedTrain("AKT:Train:1", OWNER), savedTrain("RUT:Train:9", "RUT"));

        ValidationException e = assertThrows(ValidationException.class, () -> saver.saveNewVersion(null, compound, Instant.now()));

        assertThat(e.getMessage()).contains("Component 2").contains("RUT:Train:9").contains("RUT").contains(OWNER);
        verify(defaultVersionedSaverService, never()).saveNewVersion(any(), any(), any(Instant.class), any());
    }

    @Test
    @DisplayName("A compound train component without a train, or with a train that is not saved, is rejected")
    void compoundTrainComponentWithoutExistingTrainIsRejected() {
        CompoundTrain withoutTrain = compoundTrain(OWNER, (Train) null);
        Train unsaved = train("AKT:Train:3", OWNER);
        CompoundTrain withUnsavedTrain = compoundTrain(OWNER, unsaved);

        assertThrows(ValidationException.class, () -> saver.saveNewVersion(null, withoutTrain, Instant.now()));
        assertThrows(ValidationException.class, () -> saver.saveNewVersion(null, withUnsavedTrain, Instant.now()));
    }

    private static Train train(String netexId, String owner) {
        Train train = new Train();
        train.setNetexId(netexId);
        train.setDataOwnerRef(owner);
        train.setVehicleTypeKind(VehicleTypeKindEnumeration.PHYSICAL);
        return train;
    }

    private static Train savedTrain(String netexId, String owner) {
        Train train = train(netexId, owner);
        train.setId(Math.abs((long) netexId.hashCode()));
        return train;
    }

    private static TrainElement element(String netexId, String owner) {
        TrainElement element = new TrainElement();
        element.setNetexId(netexId);
        element.setDataOwnerRef(owner);
        element.setVehicleTypeKind(VehicleTypeKindEnumeration.PHYSICAL);
        return element;
    }

    private static TrainElement savedElement(String netexId, String owner) {
        TrainElement element = element(netexId, owner);
        element.setId(Math.abs((long) netexId.hashCode()));
        return element;
    }

    private static TrainComponent component(TrainElement element) {
        TrainComponent component = new TrainComponent();
        component.setTrainElement(element);
        return component;
    }

    private static CompoundTrain compoundTrain(String owner, Train... trains) {
        CompoundTrain compound = new CompoundTrain();
        compound.setNetexId("AKT:CompoundTrain:1");
        compound.setDataOwnerRef(owner);
        compound.setVehicleTypeKind(VehicleTypeKindEnumeration.PHYSICAL);
        for (Train train : trains) {
            TrainInCompoundTrain component = new TrainInCompoundTrain();
            component.setTrain(train);
            compound.getComponents().add(component);
        }
        return compound;
    }
}

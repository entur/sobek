/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package org.rutebanken.sobek.importer.handler;

import jakarta.xml.bind.JAXBElement;
import org.entur.netex2.model.*;
import org.rutebanken.sobek.importer.ImportParams;
import org.rutebanken.sobek.importer.VehicleTypeImporter;
import org.rutebanken.sobek.importer.converter.TrainElementIdConverter;
import org.rutebanken.sobek.importer.converter.VehicleTypeIdConverter;
import org.rutebanken.sobek.model.vehicle.VehicleTypeKindEnumeration;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.VehicleTypeMapper;
import org.rutebanken.sobek.netex.util.PublicationDeliveryHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.validation.ValidationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class VehicleTypeImportHandler {

    private static final Logger logger = LoggerFactory.getLogger(VehicleTypeImportHandler.class);

    private final PublicationDeliveryHelper publicationDeliveryHelper;

    private final VehicleTypeMapper vehicleTypeMapper;

    private final VehicleTypeImporter vehicleTypeImporter;

    private final VehicleTypeIdConverter vehicleTypeIdConverter;

    private final TrainElementIdConverter trainElementIdConverter;

    private final MappingContext context;

    public VehicleTypeImportHandler(PublicationDeliveryHelper publicationDeliveryHelper,
                                    VehicleTypeMapper vehicleTypeMapper,
                                    VehicleTypeImporter vehicleTypeImporter, VehicleTypeIdConverter vehicleTypeIdConverter,
                                    TrainElementIdConverter trainElementIdConverter, MappingContext context) {
        this.publicationDeliveryHelper = publicationDeliveryHelper;
        this.vehicleTypeMapper = vehicleTypeMapper;
        this.vehicleTypeImporter = vehicleTypeImporter;
        this.vehicleTypeIdConverter = vehicleTypeIdConverter;
        this.trainElementIdConverter = trainElementIdConverter;
        this.context = context;
    }

    // One transaction for all vehicle types: they are saved one at a time, but a failure must still undo the whole file
    @Transactional
    public void handleVehicleTypes(ResourceFrame netexResourceFrame, ImportParams importParams, AtomicInteger vehicleTypesCounter, ResourceFrame responseResourceframe) {

        List<DataManagedObjectStructure> incoming = incomingVehicleTypes(netexResourceFrame);
        if (!incoming.isEmpty()) {
            logger.info("Publication delivery contains {} vehicle types for import.", incoming.size());

            // A compound train refers to the trains it is made of, and a train to the train elements it is made of. A
            // reference is resolved when the object is mapped, so an object is mapped and saved only after the ones it
            // refers to in this file are saved. (Elements first, then trains, then compound trains.)
            logger.info("About to map and import {} vehicle types", incoming.size());
            // The train elements are in a container of their own in the frame, the other vehicle types in another one
            List<VehicleType_VersionStructure> importedVehicleTypes = new ArrayList<>();
            List<TrainElementType_VersionStructure> trainElements = new ArrayList<>();
            for (DataManagedObjectStructure incomingType : dependenciesFirst(incoming)) {
                // Read the id the client sent before the converter swaps it for an already imported (e.g. CVT) id
                String clientId = incomingType.getId();
                if (incomingType instanceof TrainElement trainElement) {
                    var mapped = mapWithKind(vehicleTypeMapper.mapToSobek(trainElementIdConverter.convertIncomingId(trainElement), context), clientId, importParams);
                    trainElements.add(vehicleTypeImporter.importTrainElement((org.rutebanken.sobek.model.vehicle.TrainElement) mapped, vehicleTypesCounter));
                } else {
                    var vehicleType = (VehicleType_VersionStructure) incomingType;
                    var mapped = mapWithKind(vehicleTypeMapper.mapAnyToSobek(vehicleTypeIdConverter.convertIncomingId(vehicleType), context), clientId, importParams);
                    importedVehicleTypes.addAll(vehicleTypeImporter.importVehicleTypes(List.of(mapped), vehicleTypesCounter));
                }
            }
            logger.info("Mapped and imported {} vehicle types", importedVehicleTypes.size() + trainElements.size());

            List<JAXBElement<? extends DataManagedObjectStructure>> vehicleTypeElements = importedVehicleTypes.stream()
                    .map(VehicleTypeMapper::toJaxbElement)
                    .collect(Collectors.toList());

            if (!vehicleTypeElements.isEmpty()) {
                responseResourceframe.withVehicleTypes(
                        new VehicleTypesInFrame_RelStructure()
                                .withTransportType(vehicleTypeElements));
            }
            if (!trainElements.isEmpty()) {
                responseResourceframe.withTrainElementTypes(
                        new TrainElementTypesInFrame_RelStructure()
                                .withTrailingElementTypeOrTractiveElementTypeOrTrainElement(trainElements));
            }

            logger.info("Finished importing vehicle types");
        }
    }

    /**
     * The vehicle types, trains and compound trains of the frame, and its train elements. Other kinds of train element
     * types (trailing and tractive element types) are not stored, so a file with one is rejected and not silently
     * left out.
     */
    private List<DataManagedObjectStructure> incomingVehicleTypes(ResourceFrame frame) {
        List<DataManagedObjectStructure> incoming = new ArrayList<>();
        if (publicationDeliveryHelper.hasVehicleTypes(frame)) {
            frame.getVehicleTypes().getTransportType().stream()
                    .filter(this::isVehicleType)
                    .map(JAXBElement::getValue)
                    .forEach(incoming::add);
        }
        if (frame.getTrainElementTypes() != null) {
            for (TrainElementType_VersionStructure elementType : frame.getTrainElementTypes().getTrailingElementTypeOrTractiveElementTypeOrTrainElement()) {
                if (!(elementType instanceof TrainElement)) {
                    throw new ValidationException("Only TrainElement is supported in trainElementTypes, not "
                            + elementType.getClass().getSimpleName() + " (" + elementType.getId() + ").");
                }
                incoming.add(elementType);
            }
        }
        return incoming;
    }

    /**
     * The kind follows the id when the id the client sent is in the contractual codespace (CVT). Any other id says
     * nothing about the kind, so the kind the client asked for in the import applies. This is decided on the client's
     * original id: an external id that the converter resolved to an existing CVT id must still be checked against the
     * requested kind, so that a conflicting kind is rejected by the saver.
     */
    private org.rutebanken.sobek.model.vehicle.VehicleType mapWithKind(org.rutebanken.sobek.model.vehicle.VehicleType mapped, String clientId, ImportParams importParams) {
        if (mapped != null && importParams.getVehicleTypeKind() != null
                && VehicleTypeMapper.kindFromNetexId(clientId) == VehicleTypeKindEnumeration.PHYSICAL) {
            mapped.setVehicleTypeKind(importParams.getVehicleTypeKind());
        }
        return mapped;
    }

    private boolean isVehicleType(JAXBElement<? extends DataManagedObjectStructure> jaxbElement) {
        Object value = jaxbElement.getValue();
        return value instanceof VehicleType || value instanceof Train || value instanceof CompoundTrain;
    }

    /**
     * Orders the vehicle types so that each comes after the ones it refers to in the same list. Types that do not
     * depend on each other keep their order. A cycle cannot be saved and is rejected.
     */
    private static List<DataManagedObjectStructure> dependenciesFirst(List<DataManagedObjectStructure> vehicleTypes) {
        Set<String> idsInFile = vehicleTypes.stream().map(EntityStructure::getId).filter(Objects::nonNull).collect(Collectors.toSet());
        List<DataManagedObjectStructure> pending = new ArrayList<>(vehicleTypes);
        Set<String> doneIds = new HashSet<>();
        List<DataManagedObjectStructure> ordered = new ArrayList<>();
        while (!pending.isEmpty()) {
            var ready = pending.stream()
                    .filter(vt -> referredIds(vt).stream().noneMatch(ref -> idsInFile.contains(ref) && !doneIds.contains(ref) && !ref.equals(vt.getId())))
                    .toList();
            if (ready.isEmpty()) {
                throw new ValidationException("Trains refer to each other in a cycle: "
                        + pending.stream().map(EntityStructure::getId).collect(Collectors.joining(", ")));
            }
            ready.forEach(vt -> {
                ordered.add(vt);
                doneIds.add(vt.getId());
            });
            pending.removeAll(ready);
        }
        return ordered;
    }

    /** The ids a vehicle type refers to: the trains of a compound train, and the train elements of a train. */
    private static Set<String> referredIds(DataManagedObjectStructure vehicleType) {
        Set<String> refs = new HashSet<>();
        if (vehicleType instanceof CompoundTrain compoundTrain && compoundTrain.getComponents() != null) {
            for (var component : compoundTrain.getComponents().getTrainInCompoundTrain()) {
                if (component.getTrainRef() != null && component.getTrainRef().getValue() != null) {
                    refs.add(component.getTrainRef().getValue().getRef());
                }
            }
        } else if (vehicleType instanceof Train train && train.getComponents() != null) {
            for (Object component : train.getComponents().getTrainComponentRefOrTrainComponent()) {
                if (component instanceof TrainComponent trainComponent) {
                    if (trainComponent.getTrainElementRef() != null) {
                        refs.add(trainComponent.getTrainElementRef().getRef());
                    } else if (trainComponent.getTrainElementTypeRef() != null && trainComponent.getTrainElementTypeRef().getValue() != null) {
                        refs.add(trainComponent.getTrainElementTypeRef().getValue().getRef());
                    }
                }
            }
        }
        return refs;
    }
}

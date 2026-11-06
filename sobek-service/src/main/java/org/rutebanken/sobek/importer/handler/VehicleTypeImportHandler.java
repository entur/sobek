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

    private final MappingContext context;

    public VehicleTypeImportHandler(PublicationDeliveryHelper publicationDeliveryHelper,
                                    VehicleTypeMapper vehicleTypeMapper,
                                    VehicleTypeImporter vehicleTypeImporter, VehicleTypeIdConverter vehicleTypeIdConverter, MappingContext context) {
        this.publicationDeliveryHelper = publicationDeliveryHelper;
        this.vehicleTypeMapper = vehicleTypeMapper;
        this.vehicleTypeImporter = vehicleTypeImporter;
        this.vehicleTypeIdConverter = vehicleTypeIdConverter;
        this.context = context;
    }

    // One transaction for all vehicle types: they are saved one at a time, but a failure must still undo the whole file
    @Transactional
    public void handleVehicleTypes(ResourceFrame netexResourceFrame, ImportParams importParams, AtomicInteger vehicleTypesCounter, ResourceFrame responseResourceframe) {

        if (publicationDeliveryHelper.hasVehicleTypes(netexResourceFrame)) {
            var originalVehicleTypes = netexResourceFrame.getVehicleTypes().getTransportType();
            logger.info("Publication delivery contains {} vehicle types for import.", originalVehicleTypes.size());

            logger.info("About to check if incoming vehicle types have previously been imported with the same id");
            List<VehicleType_VersionStructure> incoming = originalVehicleTypes.stream()
                    .filter(this::isVehicleType)
                    .map(jaxbElement -> (VehicleType_VersionStructure) jaxbElement.getValue())
                    .collect(Collectors.toList());

            // A train refers to the trains it is made of, and a reference is resolved when the train is mapped. So a
            // train is mapped and saved only after the trains it refers to in this file are saved. (Only compound trains refer to trains.)
            logger.info("About to map and import {} vehicle types", incoming.size());
            List<VehicleType_VersionStructure> importedVehicleTypes = new ArrayList<>();
            for (VehicleType_VersionStructure vt : dependenciesFirst(incoming)) {
                // Read the id the client sent before the converter swaps it for an already imported (e.g. CVT) id
                String clientId = vt.getId();
                var mapped = mapWithKind(vehicleTypeIdConverter.convertIncomingId(vt), clientId, importParams);
                importedVehicleTypes.addAll(vehicleTypeImporter.importVehicleTypes(List.of(mapped), vehicleTypesCounter));
            }
            logger.info("Mapped and imported {} vehicle types", importedVehicleTypes.size());

            List<JAXBElement<? extends DataManagedObjectStructure>> vehicleTypeElements = importedVehicleTypes.stream()
                    .map(VehicleTypeMapper::toJaxbElement)
                    .collect(java.util.stream.Collectors.toList());

            responseResourceframe.withVehicleTypes(
                    new VehicleTypesInFrame_RelStructure()
                            .withTransportType(vehicleTypeElements));

            logger.info("Finished importing vehicle types");
        }
    }

    /**
     * The kind follows the id when the id the client sent is in the contractual codespace (CVT). Any other id says
     * nothing about the kind, so the kind the client asked for in the import applies. This is decided on the client's
     * original id: an external id that the converter resolved to an existing CVT id must still be checked against the
     * requested kind, so that a conflicting kind is rejected by the saver.
     */
    private org.rutebanken.sobek.model.vehicle.VehicleType mapWithKind(VehicleType_VersionStructure convertedVehicleType, String clientId, ImportParams importParams) {
        var mapped = vehicleTypeMapper.mapAnyToSobek(convertedVehicleType, context);
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
     * Orders the vehicle types so that each comes after the trains it refers to in the same list. Types that do not
     * depend on each other keep their order. A cycle cannot be saved and is rejected.
     */
    private static List<VehicleType_VersionStructure> dependenciesFirst(List<VehicleType_VersionStructure> vehicleTypes) {
        Set<String> idsInFile = vehicleTypes.stream().map(EntityStructure::getId).filter(Objects::nonNull).collect(Collectors.toSet());
        List<VehicleType_VersionStructure> pending = new ArrayList<>(vehicleTypes);
        Set<String> doneIds = new HashSet<>();
        List<VehicleType_VersionStructure> ordered = new ArrayList<>();
        while (!pending.isEmpty()) {
            var ready = pending.stream()
                    .filter(vt -> referredTrainIds(vt).stream().noneMatch(ref -> idsInFile.contains(ref) && !doneIds.contains(ref) && !ref.equals(vt.getId())))
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

    private static Set<String> referredTrainIds(VehicleType_VersionStructure vehicleType) {
        Set<String> refs = new HashSet<>();
        // Only a compound train refers to other vehicle types: its components are trains
        if (vehicleType instanceof CompoundTrain compoundTrain && compoundTrain.getComponents() != null) {
            for (var component : compoundTrain.getComponents().getTrainInCompoundTrain()) {
                if (component.getTrainRef() != null && component.getTrainRef().getValue() != null) {
                    refs.add(component.getTrainRef().getValue().getRef());
                }
            }
        }
        return refs;
    }
}

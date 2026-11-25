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

import org.entur.netex2.model.*;
import jakarta.validation.ValidationException;
import org.rutebanken.sobek.importer.ImportParams;
import org.rutebanken.sobek.netex.id.VehicleModelId;
import org.rutebanken.sobek.importer.VehicleImporter;
import org.rutebanken.sobek.importer.converter.VehicleIdConverter;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.VehicleModelMapper;
import org.rutebanken.sobek.netex.mapping.mapstruct.VehicleMapper;
import org.rutebanken.sobek.netex.util.PublicationDeliveryHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class VehicleImportHandler {

    private static final Logger logger = LoggerFactory.getLogger(VehicleImportHandler.class);

    private final PublicationDeliveryHelper publicationDeliveryHelper;
    private final VehicleMapper vehicleMapper;
    private final VehicleImporter vehicleImporter;
    private final VehicleIdConverter vehicleIdConverter;
    private final MappingContext context;
    private final VehicleModelMapper vehicleModelMapper;

    public VehicleImportHandler(PublicationDeliveryHelper publicationDeliveryHelper, VehicleMapper vehicleMapper, VehicleImporter vehicleImporter, VehicleIdConverter vehicleIdConverter, MappingContext context, VehicleModelMapper vehicleModelMapper) {
        this.publicationDeliveryHelper = publicationDeliveryHelper;
        this.vehicleMapper = vehicleMapper;
        this.vehicleImporter = vehicleImporter;
        this.vehicleIdConverter = vehicleIdConverter;
        this.context = context;
        this.vehicleModelMapper = vehicleModelMapper;
    }

    public void handleVehicles(ResourceFrame netexResourceFrame, ImportParams importParams, AtomicInteger vehiclesCounter, ResourceFrame responseResourceFrame) {
        if (publicationDeliveryHelper.hasVehicles(netexResourceFrame)) {
            var originalVehicles = netexResourceFrame.getVehicles().getVehicle();
            logger.info("Publication delivery contains {} vehicles for import.", originalVehicles.size());

            logger.info("About to check if incoming vehicles have previously been imported with the same id");
            var originalWithMappedIds = originalVehicles.stream()
                    .map(vehicleIdConverter::convertIncomingId)
                    .toList();

            logger.info("About to map {} vehicles to internal model", netexResourceFrame.getVehicles().getVehicle().size());
            List<org.rutebanken.sobek.model.vehicle.Vehicle> mappedVehicles = vehicleMapper
                    .mapAsList(originalWithMappedIds, context);
            logger.info("Mapped {} vehicles to internal model", mappedVehicles.size());

            VehicleModelAssociations models = VehicleModelAssociations.of(netexResourceFrame);
            for (int i = 0; i < mappedVehicles.size(); i++) {
                checkVehicleModelRef(originalWithMappedIds.get(i), mappedVehicles.get(i), models);
            }
            List<Vehicle> importedVehicles = vehicleImporter.importVehicles(mappedVehicles, vehiclesCounter);
            responseResourceFrame.withVehicles(new VehiclesInFrame_RelStructure().withVehicle(importedVehicles));
            // The vehicles refer to the models of their types: the response has them, as the export does
            mappedVehicles.stream()
                    .map(org.rutebanken.sobek.model.vehicle.Vehicle::getTransportType)
                    .filter(java.util.Objects::nonNull)
                    .forEach(type -> vehicleModelMapper.addToFrame(responseResourceFrame, type));
            logger.info("Finished importing vehicles");
        }
    }

    /**
     * A vehicle has no vehicle model of its own: its model is the one of its vehicle type. A model reference on an
     * incoming vehicle is therefore accepted only if it is the model of the type of the vehicle: the model id derived
     * from the id of the type, as exported, or the id of the model the same delivery gives the type. Anything else says
     * the vehicle has a model it cannot have, and is rejected.
     */
    private static void checkVehicleModelRef(org.entur.netex2.model.Vehicle incoming,
                                             org.rutebanken.sobek.model.vehicle.Vehicle mapped,
                                             VehicleModelAssociations models) {
        if (incoming.getVehicleModelRef() == null || incoming.getVehicleModelRef().getRef() == null) {
            return;
        }
        String modelRef = incoming.getVehicleModelRef().getRef();
        org.rutebanken.sobek.model.vehicle.VehicleType type = mapped.getTransportType();
        String typeRef = incoming.getTransportTypeRef() != null && incoming.getTransportTypeRef().getValue() != null
                ? incoming.getTransportTypeRef().getValue().getRef() : null;

        if (type == null || !type.hasVehicleModelData()) {
            throw new ValidationException("The vehicle " + incoming.getId() + " refers to the vehicle model " + modelRef
                    + ", but its vehicle type has no vehicle model. The vehicle model of a vehicle is the one of its vehicle type.");
        }
        boolean isModelOfType = modelRef.equals(VehicleModelId.forVehicleType(type.getNetexId()))
                || models.typeIdOfModel(modelRef).filter(typeId -> typeId.equals(typeRef) || typeId.equals(type.getNetexId())).isPresent();
        if (!isModelOfType) {
            throw new ValidationException("The vehicle " + incoming.getId() + " refers to the vehicle model " + modelRef
                    + ", which is not the vehicle model of its vehicle type " + type.getNetexId() + ".");
        }
    }
}

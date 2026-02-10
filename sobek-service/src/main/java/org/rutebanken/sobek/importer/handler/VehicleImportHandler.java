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

import org.rutebanken.netex.model.*;
import org.rutebanken.sobek.importer.ImportParams;
import org.rutebanken.sobek.importer.VehicleImporter;
import org.rutebanken.sobek.importer.converter.VehicleIdConverter;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.VehicleMapper;
import org.rutebanken.sobek.netex.mapping.PublicationDeliveryHelper;
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

    public VehicleImportHandler(PublicationDeliveryHelper publicationDeliveryHelper, VehicleMapper vehicleMapper, VehicleImporter vehicleImporter, VehicleIdConverter vehicleIdConverter, MappingContext context) {
        this.publicationDeliveryHelper = publicationDeliveryHelper;
        this.vehicleMapper = vehicleMapper;
        this.vehicleImporter = vehicleImporter;
        this.vehicleIdConverter = vehicleIdConverter;
        this.context = context;
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
            List<Vehicle> importedVehicles = vehicleImporter.importVehicles(mappedVehicles, vehiclesCounter);
            responseResourceFrame.withVehicles(new VehiclesInFrame_RelStructure().withVehicle(importedVehicles));
            logger.info("Finished importing vehicles");
        }
    }
}

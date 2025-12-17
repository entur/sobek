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

package org.rutebanken.sobek.importer;

import org.rutebanken.netex.model.PublicationDeliveryStructure;
import org.rutebanken.netex.model.ResourceFrame;
import org.rutebanken.netex.model.SiteFrame;
import org.rutebanken.sobek.auth.AuthorizationService;
import org.rutebanken.sobek.exporter.PublicationDeliveryCreator;
import org.rutebanken.sobek.importer.handler.*;
import org.rutebanken.sobek.importer.log.ImportLogger;
import org.rutebanken.sobek.importer.log.ImportLoggerTask;
import org.rutebanken.sobek.netex.mapping.NetexMapper;
import org.rutebanken.sobek.netex.mapping.PublicationDeliveryHelper;
import org.rutebanken.sobek.service.batch.BackgroundJobs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;

import static org.rutebanken.sobek.netex.mapping.NetexMappingContextThreadLocal.updateMappingContext;

@Service
public class PublicationDeliveryImporter {

    private static final Logger logger = LoggerFactory.getLogger(PublicationDeliveryImporter.class);

    public static final String IMPORT_CORRELATION_ID = "importCorrelationId";


    private final PublicationDeliveryHelper publicationDeliveryHelper;
    private final PublicationDeliveryCreator publicationDeliveryCreator;
    private final VehicleImportHandler vehicleImportHandler;
    private final VehicleTypeImportHandler vehicleTypeImportHandler;
    private final DeckPlanImportHandler deckPlanImportHandler;
    private final EquipmentImportHandler equipmentImportHandler;
    private final VehicleModelImportHandler vehicleModelImportHandler;
    private final BackgroundJobs backgroundJobs;
    private final AuthorizationService authorizationService;
    private final boolean authorizationEnabled;

    @Autowired
    public PublicationDeliveryImporter(PublicationDeliveryHelper publicationDeliveryHelper, NetexMapper netexMapper,
                                       PublicationDeliveryCreator publicationDeliveryCreator,
                                       VehicleImportHandler vehicleImportHandler, VehicleTypeImportHandler vehicleTypeImportHandler, DeckPlanImportHandler deckPlanImportHandler, EquipmentImportHandler equipmentImportHandler, VehicleModelImportHandler vehicleModelImportHandler,
                                       BackgroundJobs backgroundJobs,
                                       AuthorizationService authorizationService,
                                       @Value("${authorization.enabled:true}") boolean authorizationEnabled) {
        this.publicationDeliveryHelper = publicationDeliveryHelper;
        this.publicationDeliveryCreator = publicationDeliveryCreator;
        this.vehicleImportHandler = vehicleImportHandler;
        this.vehicleTypeImportHandler = vehicleTypeImportHandler;
        this.deckPlanImportHandler = deckPlanImportHandler;
        this.equipmentImportHandler = equipmentImportHandler;
        this.vehicleModelImportHandler = vehicleModelImportHandler;
        this.backgroundJobs = backgroundJobs;
        this.authorizationService = authorizationService;
        this.authorizationEnabled = authorizationEnabled;
    }


    public PublicationDeliveryStructure importPublicationDelivery(PublicationDeliveryStructure incomingPublicationDelivery) {
        return importPublicationDelivery(incomingPublicationDelivery, null);
    }

    @SuppressWarnings("unchecked")
    public PublicationDeliveryStructure importPublicationDelivery(PublicationDeliveryStructure incomingPublicationDelivery, ImportParams importParams) {
        if(authorizationEnabled && !authorizationService.canEditAllEntities()){
            throw new AccessDeniedException("Insufficient privileges for operation");
        }


        if (incomingPublicationDelivery.getDataObjects() == null) {
            String responseMessage = "Received publication delivery but it does not contain any data objects.";
            logger.warn(responseMessage);
            throw new RuntimeException(responseMessage);
        }

        if (importParams == null) {
            importParams = new ImportParams();
        } else {
            validate(importParams);
        }

        logger.info("Got publication delivery with {} site frames and description {}",
                incomingPublicationDelivery.getDataObjects().getCompositeFrameOrCommonFrame().size(),
                incomingPublicationDelivery.getDescription());

        AtomicInteger vehicleCounter = new AtomicInteger(0);
        AtomicInteger vehicleTypeCounter = new AtomicInteger(0);
        AtomicInteger vehicleModelCounter = new AtomicInteger(0);
        AtomicInteger deckPlanCounter = new AtomicInteger(0);
        AtomicInteger equipmentCounter = new AtomicInteger(0);

        // Currently only supporting one resource frame per publication delivery
        ResourceFrame netexResourceFrame = publicationDeliveryHelper.findResourceFrame(incomingPublicationDelivery);

        updateMappingContext(incomingPublicationDelivery);

        ResourceFrame responseResourceFrame = null;
        if(netexResourceFrame != null) {
            String requestId = netexResourceFrame.getId();
            Timer loggerTimer = new ImportLogger(new ImportLoggerTask(vehicleCounter, publicationDeliveryHelper.numberOfVehicles(netexResourceFrame), vehicleTypeCounter, netexResourceFrame.getId()));
            try {
                responseResourceFrame = new ResourceFrame();

                MDC.put(IMPORT_CORRELATION_ID, requestId);
                logger.info("Publication delivery contains resource frame created at {}", netexResourceFrame.getCreated());

                responseResourceFrame.withId(requestId + "-response").withVersion("1");
                if(netexResourceFrame.getEquipments() != null) {
                    equipmentImportHandler.handleEquipments(netexResourceFrame, importParams, equipmentCounter, responseResourceFrame);
                }
                if(netexResourceFrame.getDeckPlans() != null) {
                    deckPlanImportHandler.handleDeckPlans(netexResourceFrame, importParams, deckPlanCounter, responseResourceFrame);
                }
                if(netexResourceFrame.getVehicleModels() != null) {
                    vehicleModelImportHandler.handleVehicleModels(netexResourceFrame, importParams, vehicleModelCounter, responseResourceFrame);
                }
                if(netexResourceFrame.getVehicleTypes() != null) {
                    vehicleTypeImportHandler.handleVehicleTypes(netexResourceFrame, importParams, vehicleTypeCounter, responseResourceFrame);
                }
                if(netexResourceFrame.getVehicles() != null) {
                    vehicleImportHandler.handleVehicles(netexResourceFrame, importParams, vehicleCounter, responseResourceFrame);
                }
            } finally {
                MDC.remove(IMPORT_CORRELATION_ID);
                loggerTimer.cancel();
            }
        }
        return publicationDeliveryCreator.createPublicationDelivery(responseResourceFrame);
    }


    private void validate(ImportParams importParams) {
    }

}

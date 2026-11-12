package org.rutebanken.sobek.importer.handler;

import org.entur.netex2.model.Fleets_RelStructure;
import org.entur.netex2.model.MobilityServiceFrame;
import org.rutebanken.sobek.importer.FleetImporter;
import org.rutebanken.sobek.importer.converter.FleetIdConverter;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.FleetMapper;
import org.rutebanken.sobek.netex.util.PublicationDeliveryHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Imports the fleets of a MobilityServiceFrame. The fleets refer to vehicles and vehicle types, so they are imported
 * after those, and the response has them in a MobilityServiceFrame of its own.
 */
@Component
public class FleetImportHandler {

    private static final Logger logger = LoggerFactory.getLogger(FleetImportHandler.class);

    private final PublicationDeliveryHelper publicationDeliveryHelper;
    private final FleetMapper fleetMapper;
    private final FleetImporter fleetImporter;
    private final FleetIdConverter fleetIdConverter;
    private final MappingContext context;

    public FleetImportHandler(PublicationDeliveryHelper publicationDeliveryHelper, FleetMapper fleetMapper, FleetImporter fleetImporter, FleetIdConverter fleetIdConverter, MappingContext context) {
        this.publicationDeliveryHelper = publicationDeliveryHelper;
        this.fleetMapper = fleetMapper;
        this.fleetImporter = fleetImporter;
        this.fleetIdConverter = fleetIdConverter;
        this.context = context;
    }

    public void handleFleets(MobilityServiceFrame netexMobilityServiceFrame, AtomicInteger fleetsCounter, MobilityServiceFrame responseMobilityServiceFrame) {
        if (publicationDeliveryHelper.hasFleets(netexMobilityServiceFrame)) {
            var originalFleets = netexMobilityServiceFrame.getFleets().getFleet();
            logger.info("Publication delivery contains {} fleets for import.", originalFleets.size());

            var originalWithMappedIds = originalFleets.stream()
                    .map(fleetIdConverter::convertIncomingId)
                    .toList();

            List<org.rutebanken.sobek.model.vehicle.Fleet> mappedFleets = fleetMapper.mapAsList(originalWithMappedIds, context);
            logger.info("Mapped {} fleets to internal model", mappedFleets.size());

            var importedFleets = fleetImporter.importFleets(mappedFleets, fleetsCounter);
            responseMobilityServiceFrame.withFleets(new Fleets_RelStructure().withFleet(importedFleets));
            logger.info("Finished importing fleets");
        }
    }
}

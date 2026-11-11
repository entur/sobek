package org.rutebanken.sobek.importer;

import org.rutebanken.sobek.model.vehicle.Fleet;
import org.rutebanken.sobek.netex.mapping.context.MappingContext;
import org.rutebanken.sobek.netex.mapping.mapstruct.FleetMapper;
import org.rutebanken.sobek.versioning.save.FleetVersionedSaverService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;

@Transactional
@Component
public class FleetImporter {

    private static final Logger logger = LoggerFactory.getLogger(FleetImporter.class);

    private final FleetMapper fleetMapper;

    private final FleetVersionedSaverService fleetVersionedSaverService;

    private final MappingContext context;

    @Autowired
    public FleetImporter(FleetMapper fleetMapper, FleetVersionedSaverService fleetVersionedSaverService, MappingContext context) {
        this.fleetMapper = fleetMapper;
        this.fleetVersionedSaverService = fleetVersionedSaverService;
        this.context = context;
    }

    public List<org.entur.netex2.model.Fleet> importFleets(List<Fleet> fleets, AtomicInteger fleetsCounter) {

        logger.info("Importing {} incoming fleets", fleets.size());

        List<Fleet> result = new ArrayList<>();

        for (Fleet incomingFleet : fleets) {
            logger.debug("{}", incomingFleet);
            result.add(fleetVersionedSaverService.saveNewVersion(incomingFleet));
            fleetsCounter.incrementAndGet();
        }

        return result.stream().map(fleet -> fleetMapper.mapToNetex(fleet, context)).collect(toList());
    }
}

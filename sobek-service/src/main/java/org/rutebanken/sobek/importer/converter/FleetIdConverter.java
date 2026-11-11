package org.rutebanken.sobek.importer.converter;

import org.rutebanken.sobek.model.vehicle.Fleet;
import org.rutebanken.sobek.netex.id.NetexIdHelper;
import org.rutebanken.sobek.netex.id.ValidPrefixList;
import org.rutebanken.sobek.repository.generic.GenericEntityInVersionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Matches fleets from the Sobek ID or the imported/original ID
 */
@Transactional
@Component
public class FleetIdConverter extends GenericIdConverter<org.entur.netex2.model.Fleet> {

    public FleetIdConverter(ValidPrefixList validPrefixList, NetexIdHelper netexIdHelper, GenericEntityInVersionRepository genericEntityInVersionRepository) {
        super(validPrefixList, netexIdHelper, genericEntityInVersionRepository, Fleet.class);
    }
}

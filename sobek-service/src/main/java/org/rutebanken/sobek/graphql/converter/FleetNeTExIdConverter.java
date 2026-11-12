package org.rutebanken.sobek.graphql.converter;

import org.rutebanken.sobek.model.vehicle.Fleet;
import org.rutebanken.sobek.netex.id.NetexIdHelper;
import org.rutebanken.sobek.netex.id.ValidPrefixList;
import org.rutebanken.sobek.repository.generic.GenericEntityInVersionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Matches fleets from the Sobek ID or imported/original ID
 */
@Transactional
@Component
public class FleetNeTExIdConverter extends GenericNeTExIdConverter<Fleet> {

    public FleetNeTExIdConverter(ValidPrefixList validPrefixList, NetexIdHelper netexIdHelper, GenericEntityInVersionRepository genericEntityInVersionRepository) {
        super(validPrefixList, netexIdHelper, genericEntityInVersionRepository, Fleet.class);
    }
}

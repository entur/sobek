package org.rutebanken.sobek.importer.converter;

import org.entur.netex2.model.TrainElement;
import org.rutebanken.sobek.model.vehicle.VehicleType;
import org.rutebanken.sobek.netex.id.NetexIdHelper;
import org.rutebanken.sobek.netex.id.ValidPrefixList;
import org.rutebanken.sobek.repository.generic.GenericEntityInVersionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Matches train elements from nsr ID or imported/original ID. A train element is a vehicle type in Sobek, so its ids
 * are matched among the vehicle types. In NeTEx it is not a vehicle type, so it needs a converter of its own.
 */
@Transactional
@Component
public class TrainElementIdConverter extends GenericIdConverter<TrainElement> {

    public TrainElementIdConverter(ValidPrefixList validPrefixList, NetexIdHelper netexIdHelper, GenericEntityInVersionRepository genericEntityInVersionRepository) {
        super(validPrefixList, netexIdHelper, genericEntityInVersionRepository, VehicleType.class);
    }
}

package org.rutebanken.sobek.netex.mapping.mapper;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.rutebanken.netex.model.PassengerSpot;
import org.rutebanken.netex.model.SpotRowRefStructure;

public class PassengerSpotMapper extends CustomMapper<PassengerSpot, org.rutebanken.sobek.model.vehicle.PassengerSpot> {

    @Override
    public void mapAtoB(PassengerSpot netexPassengerSpot, org.rutebanken.sobek.model.vehicle.PassengerSpot sobekPassengerSpot, MappingContext context) {
        super.mapAtoB(netexPassengerSpot, sobekPassengerSpot, context);
    }

    @Override
    public void mapBtoA(org.rutebanken.sobek.model.vehicle.PassengerSpot sobekPassengerSpot, PassengerSpot netexPassengerSpot, MappingContext context) {
        super.mapBtoA(sobekPassengerSpot, netexPassengerSpot, context);

        if (sobekPassengerSpot.getName() != null) {
            netexPassengerSpot.getName().withContent(sobekPassengerSpot.getName().getValue());
        }

        if (sobekPassengerSpot.getDescription() != null) {
            netexPassengerSpot.getDescription().withContent(sobekPassengerSpot.getDescription().getValue());
        }

        if (sobekPassengerSpot.getLabel() != null) {
            netexPassengerSpot.getLabel().withContent(sobekPassengerSpot.getLabel().getValue());
        }

        if (sobekPassengerSpot.getSpotRowRef() != null) {
            netexPassengerSpot.withSpotRowRef(new SpotRowRefStructure().withRef(sobekPassengerSpot.getSpotRowRef()));
        }
    }
}

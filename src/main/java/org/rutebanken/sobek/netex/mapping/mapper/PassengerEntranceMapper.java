package org.rutebanken.sobek.netex.mapping.mapper;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.rutebanken.netex.model.PassengerEntrance;

public class PassengerEntranceMapper extends CustomMapper<PassengerEntrance, org.rutebanken.sobek.model.vehicle.PassengerEntrance> {

    @Override
    public void mapAtoB(PassengerEntrance netexPassengerEntrance, org.rutebanken.sobek.model.vehicle.PassengerEntrance sobekPassengerEntrance, MappingContext context) {
        super.mapAtoB(netexPassengerEntrance, sobekPassengerEntrance, context);
    }

    @Override
    public void mapBtoA(org.rutebanken.sobek.model.vehicle.PassengerEntrance sobekPassengerEntrance, PassengerEntrance netexPassengerEntrance, MappingContext context) {
        super.mapBtoA(sobekPassengerEntrance, netexPassengerEntrance, context);

        if (sobekPassengerEntrance.getName() != null) {
            netexPassengerEntrance.getName().withContent(sobekPassengerEntrance.getName().getValue());
        }

        if (sobekPassengerEntrance.getDescription() != null) {
            netexPassengerEntrance.getDescription().withContent(sobekPassengerEntrance.getDescription().getValue());
        }

        if (sobekPassengerEntrance.getLabel() != null) {
            netexPassengerEntrance.getLabel().withContent(sobekPassengerEntrance.getLabel().getValue());
        }
    }
}

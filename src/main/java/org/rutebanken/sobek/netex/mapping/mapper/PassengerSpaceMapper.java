package org.rutebanken.sobek.netex.mapping.mapper;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.rutebanken.netex.model.PassengerSpace;

public class PassengerSpaceMapper extends CustomMapper<PassengerSpace, org.rutebanken.sobek.model.vehicle.PassengerSpace> {

    @Override
    public void mapAtoB(PassengerSpace netexPassengerSpace, org.rutebanken.sobek.model.vehicle.PassengerSpace sobekPassengerSpace, MappingContext context) {
        super.mapAtoB(netexPassengerSpace, sobekPassengerSpace, context);
    }

    @Override
    public void mapBtoA(org.rutebanken.sobek.model.vehicle.PassengerSpace sobekPassengerSpace, PassengerSpace netexPassengerSpace, MappingContext context) {
        super.mapBtoA(sobekPassengerSpace, netexPassengerSpace, context);

        if (sobekPassengerSpace.getName() != null) {
            netexPassengerSpace.getName().withContent(sobekPassengerSpace.getName().getValue());
        }

        if (sobekPassengerSpace.getDescription() != null) {
            netexPassengerSpace.getDescription().withContent(sobekPassengerSpace.getDescription().getValue());
        }

        if (sobekPassengerSpace.getLabel() != null) {
            netexPassengerSpace.getLabel().withContent(sobekPassengerSpace.getLabel().getValue());
        }
    }
}

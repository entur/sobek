package org.rutebanken.sobek.netex.mapping.mapper;

import jakarta.xml.bind.JAXBElement;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.rutebanken.netex.model.PassengerSpace;

import java.util.List;

public class PassengerSpaceMapper extends CustomMapper<PassengerSpace, org.rutebanken.sobek.model.vehicle.PassengerSpace> {

    @Override
    public void mapAtoB(PassengerSpace netexPassengerSpace, org.rutebanken.sobek.model.vehicle.PassengerSpace sobekPassengerSpace, MappingContext context) {
        super.mapAtoB(netexPassengerSpace, sobekPassengerSpace, context);
        if (netexPassengerSpace.getDeckEntrances() != null &&
                netexPassengerSpace.getDeckEntrances().getDeckEntranceRefOrDeckEntrance_Dummy() != null &&
                !netexPassengerSpace.getDeckEntrances().getDeckEntranceRefOrDeckEntrance_Dummy().isEmpty()) {
            var rawDeckEntrances = netexPassengerSpace.getDeckEntrances().getDeckEntranceRefOrDeckEntrance_Dummy().stream().map(JAXBElement::getValue).toList();
            List<org.rutebanken.sobek.model.vehicle.PassengerEntrance> netexPassengerEntrances = mapperFacade.mapAsList(rawDeckEntrances, org.rutebanken.sobek.model.vehicle.PassengerEntrance.class, context);
            if (!netexPassengerEntrances.isEmpty()) {
                sobekPassengerSpace.setDeckEntrances(netexPassengerEntrances);
            }
        }

        if (netexPassengerSpace.getPassengerSpots() != null &&
                netexPassengerSpace.getPassengerSpots().getPassengerSpotRefOrPassengerSpot() != null &&
                !netexPassengerSpace.getPassengerSpots().getPassengerSpotRefOrPassengerSpot().isEmpty()) {
            List<org.rutebanken.sobek.model.vehicle.PassengerSpot> netexPassengerSpots = mapperFacade.mapAsList(netexPassengerSpace.getPassengerSpots().getPassengerSpotRefOrPassengerSpot(), org.rutebanken.sobek.model.vehicle.PassengerSpot.class, context);
            if (!netexPassengerSpots.isEmpty()) {
                sobekPassengerSpace.setPassengerSpots(netexPassengerSpots);
            }
        }
    }

    @Override
    public void mapBtoA(org.rutebanken.sobek.model.vehicle.PassengerSpace sobekPassengerSpace, PassengerSpace netexPassengerSpace, MappingContext context) {
        super.mapBtoA(sobekPassengerSpace, netexPassengerSpace, context);
    }
}

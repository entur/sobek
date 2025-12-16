package org.rutebanken.sobek.netex.mapping.mapper;

import jakarta.xml.bind.JAXBElement;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.rutebanken.netex.model.*;

import java.util.List;

public class DeckMapper extends CustomMapper<Deck, org.rutebanken.sobek.model.vehicle.Deck> {

    @Override
    public void mapAtoB(Deck netexDeck, org.rutebanken.sobek.model.vehicle.Deck sobekDeck, MappingContext context) {
        super.mapAtoB(netexDeck, sobekDeck, context);
        if (netexDeck.getDeckSpaces() != null &&
                netexDeck.getDeckSpaces().getDeckSpaceRefOrDeckSpace_Dummy() != null &&
                !netexDeck.getDeckSpaces().getDeckSpaceRefOrDeckSpace_Dummy().isEmpty()) {
            var rawDeckSpaces = netexDeck.getDeckSpaces().getDeckSpaceRefOrDeckSpace_Dummy().stream().map(JAXBElement::getValue).toList();
            List<org.rutebanken.sobek.model.vehicle.PassengerSpace> netexDeckSpaces = mapperFacade.mapAsList(rawDeckSpaces, org.rutebanken.sobek.model.vehicle.PassengerSpace.class, context);
            if (!netexDeckSpaces.isEmpty()) {
                sobekDeck.setDeckSpaces(netexDeckSpaces);
            }
        }
    }

    @Override
    public void mapBtoA(org.rutebanken.sobek.model.vehicle.Deck sobekDeck, Deck netexDeck, MappingContext context) {
        super.mapBtoA(sobekDeck, netexDeck, context);
//        if(sobekDeck.getDeckSpaces().getPassengerCapacity() != null) {
//            netexVehicleType.withPassengerCapacity(mapperFacade.map(sobekVehicleType.getPassengerCapacity(), org.rutebanken.netex.model.PassengerCapacityStructure.class, context));
//        }
    }
}

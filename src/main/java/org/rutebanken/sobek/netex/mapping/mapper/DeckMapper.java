package org.rutebanken.sobek.netex.mapping.mapper;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.rutebanken.netex.model.Deck;

public class DeckMapper extends CustomMapper<Deck, org.rutebanken.sobek.model.vehicle.Deck> {

    @Override
    public void mapAtoB(Deck netexDeck, org.rutebanken.sobek.model.vehicle.Deck sobekDeck, MappingContext context) {
        super.mapAtoB(netexDeck, sobekDeck, context);
    }

    @Override
    public void mapBtoA(org.rutebanken.sobek.model.vehicle.Deck sobekDeck, Deck netexDeck, MappingContext context) {
        super.mapBtoA(sobekDeck, netexDeck, context);

        if (sobekDeck.getName() != null) {
            netexDeck.getName().withContent(sobekDeck.getName().getValue());
        }

        if (sobekDeck.getDescription() != null) {
            netexDeck.getDescription().withContent(sobekDeck.getDescription().getValue());
        }

        if (sobekDeck.getLabel() != null) {
            netexDeck.getLabel().withContent(sobekDeck.getLabel().getValue());
        }
    }
}

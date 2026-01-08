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
        context.setProperty("currentSobekDeck", sobekDeck);

        if (netexDeck.getSpotRows() != null &&
                netexDeck.getSpotRows().getSpotRow() != null &&
                !netexDeck.getSpotRows().getSpotRow().isEmpty()) {
            List<org.rutebanken.sobek.model.vehicle.SpotRow> sobekSpotRows = mapperFacade.mapAsList(netexDeck.getSpotRows().getSpotRow(), org.rutebanken.sobek.model.vehicle.SpotRow.class, context);
            if (!sobekSpotRows.isEmpty()) {
                sobekDeck.setSpotRows(sobekSpotRows);
            }
        }
        if (netexDeck.getSpotColumns() != null &&
                netexDeck.getSpotColumns().getSpotColumn() != null &&
                !netexDeck.getSpotColumns().getSpotColumn().isEmpty()) {
            List<org.rutebanken.sobek.model.vehicle.SpotColumn> sobekSpotColumns = mapperFacade.mapAsList(netexDeck.getSpotColumns().getSpotColumn(), org.rutebanken.sobek.model.vehicle.SpotColumn.class, context);
            if (!sobekSpotColumns.isEmpty()) {
                sobekDeck.setSpotColumns(sobekSpotColumns);
            }
        }
        if (netexDeck.getDeckSpaces() != null &&
                netexDeck.getDeckSpaces().getDeckSpaceRefOrDeckSpace_Dummy() != null &&
                !netexDeck.getDeckSpaces().getDeckSpaceRefOrDeckSpace_Dummy().isEmpty()) {
            var rawDeckSpaces = netexDeck.getDeckSpaces().getDeckSpaceRefOrDeckSpace_Dummy().stream().map(JAXBElement::getValue).toList();
            List<org.rutebanken.sobek.model.vehicle.PassengerSpace> sobekDeckSpaces = mapperFacade.mapAsList(rawDeckSpaces, org.rutebanken.sobek.model.vehicle.PassengerSpace.class, context);
            if (!sobekDeckSpaces.isEmpty()) {
                for (var space : sobekDeckSpaces) {
                    // Map the parent deck space reference, see PassengerSpaceMapper.mapAtoB
                    if (space.getParentDeckSpaceRefNotMapped() != null) {
                        sobekDeckSpaces
                                .stream()
                                .filter(s -> s.getIncomingId()
                                                                .equals(space.getParentDeckSpaceRefNotMapped())).
                                findFirst()
                                .ifPresent(s -> space.setParentDeckSpace(s));
                    }
                }
                sobekDeck.setDeckSpaces(sobekDeckSpaces);
            }
        }
    }

    @Override
    public void mapBtoA(org.rutebanken.sobek.model.vehicle.Deck sobekDeck, Deck netexDeck, MappingContext context) {
        super.mapBtoA(sobekDeck, netexDeck, context);
    }
}

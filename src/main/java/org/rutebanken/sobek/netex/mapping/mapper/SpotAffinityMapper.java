package org.rutebanken.sobek.netex.mapping.mapper;

import jakarta.xml.bind.JAXBElement;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.rutebanken.netex.model.*;
import org.rutebanken.sobek.model.vehicle.LocatableSpot;

import java.util.List;
import java.util.Objects;

public class SpotAffinityMapper extends CustomMapper<SpotAffinity, org.rutebanken.sobek.model.vehicle.SpotAffinity> {

    @Override
    public void mapAtoB(SpotAffinity netexSpotAffinity, org.rutebanken.sobek.model.vehicle.SpotAffinity sobekSpotAffinity, MappingContext context) {
        super.mapAtoB(netexSpotAffinity, sobekSpotAffinity, context);
        if(netexSpotAffinity.getMembers() != null &&
                netexSpotAffinity.getMembers().getLocatableSpotRef() != null &&
                !netexSpotAffinity.getMembers().getLocatableSpotRef().isEmpty()) {
            org.rutebanken.sobek.model.vehicle.Deck deck = (org.rutebanken.sobek.model.vehicle.Deck) context.getProperty("currentSobekDeck");
            if (deck != null && deck.getSpotColumns() != null) {
                var rawMembers = netexSpotAffinity.getMembers().getLocatableSpotRef().stream().map(JAXBElement::getValue).toList();
                List<LocatableSpot> sobekSpots = rawMembers.stream().map(this::mapNeTEx2Sobek).toList();
                if(!sobekSpots.isEmpty()) {
                    sobekSpotAffinity.setMembers(sobekSpots);
                }
            }
        }

    }

    @Override
    public void mapBtoA(org.rutebanken.sobek.model.vehicle.SpotAffinity sobekSpotAffinity, SpotAffinity netexSpotAffinity, MappingContext context) {
        super.mapBtoA(sobekSpotAffinity, netexSpotAffinity, context);
        var sobekMembers = sobekSpotAffinity.getMembers();
        if(sobekMembers != null && !sobekMembers.isEmpty()) {
            LocatableSpotRefs_RelStructure members = new LocatableSpotRefs_RelStructure();
            members.getLocatableSpotRef().addAll(sobekMembers.stream()
                    .map(this::mapToNeTEx)
                    .filter(Objects::nonNull)
                    .toList());
            netexSpotAffinity.setMembers(members);
        }
    }

    private JAXBElement<? extends LocatableSpotRefStructure> mapToNeTEx(LocatableSpot spot) {
        ObjectFactory objectFactory = new ObjectFactory();
        return switch (spot) {
            case org.rutebanken.sobek.model.vehicle.PassengerSpot passengerSpot -> objectFactory.createPassengerSpotRef(new PassengerSpotRefStructure().withRef(passengerSpot.getNetexId()));
            case org.rutebanken.sobek.model.vehicle.LuggageSpot luggageSpot -> objectFactory.createLuggageSpotRef(new LuggageSpotRefStructure().withRef(luggageSpot.getNetexId()));
            default -> null;
        };
    }

    private LocatableSpot mapNeTEx2Sobek(LocatableSpotRefStructure locatableSpotRefStructure) {
        if (locatableSpotRefStructure.getRef() == null || locatableSpotRefStructure.getRef().isEmpty()) {
            return null;
        }

        return switch (locatableSpotRefStructure) {
            case org.rutebanken.netex.model.PassengerSpotRefStructure passengerSpotRefStructure ->
                    mapperFacade.map(passengerSpotRefStructure, org.rutebanken.sobek.model.vehicle.PassengerSpot.class);
            case org.rutebanken.netex.model.LuggageSpotRefStructure luggageSpotRefStructure ->
                    mapperFacade.map(luggageSpotRefStructure, org.rutebanken.sobek.model.vehicle.LuggageSpot.class);
            default -> null;
        };
    }
}

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
            org.rutebanken.sobek.model.vehicle.PassengerSpace passengerSpace = (org.rutebanken.sobek.model.vehicle.PassengerSpace) context.getProperty("currentSobekPassengerSpace");
            if (passengerSpace != null && passengerSpace.getPassengerSpots() != null) {
                var rawMembers = netexSpotAffinity.getMembers().getLocatableSpotRef().stream().map(JAXBElement::getValue).toList();
                List<LocatableSpot> sobekSpots = rawMembers.stream().map(n -> mapNeTEx2Sobek(n, passengerSpace.getPassengerSpots(), passengerSpace.getLuggageSpots())).toList();
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

    private LocatableSpot mapNeTEx2Sobek(LocatableSpotRefStructure locatableSpotRefStructure, List<org.rutebanken.sobek.model.vehicle.PassengerSpot> passengerSpots, List<org.rutebanken.sobek.model.vehicle.LuggageSpot> luggageSpots) {
        if (locatableSpotRefStructure.getRef() == null || locatableSpotRefStructure.getRef().isEmpty()) {
            return null;
        }

        var locatableSpots = switch (locatableSpotRefStructure) {
            case org.rutebanken.netex.model.PassengerSpotRefStructure passengerSpotRefStructure ->
                    passengerSpots;
            case org.rutebanken.netex.model.LuggageSpotRefStructure luggageSpotRefStructure ->
                    luggageSpots;
            default -> null;
        };
        if(locatableSpots == null) {
            return null;
        }
        return locatableSpots.stream()
                .filter(spot -> spot.getNetexId().equals(locatableSpotRefStructure.getRef()))
                .findFirst()
                .orElse(null);
    }
}

package org.rutebanken.sobek.netex.mapping.mapper;

import jakarta.xml.bind.JAXBElement;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.rutebanken.netex.model.*;
import org.rutebanken.sobek.model.vehicle.DeckSpace;
import org.rutebanken.sobek.model.vehicle.Equipment;
import org.rutebanken.sobek.model.vehicle.LocatableSpot;
import org.rutebanken.sobek.netex.mapping.EquipmentMappingHelper;

import java.util.List;

public class PassengerSpaceMapper extends CustomMapper<PassengerSpace, org.rutebanken.sobek.model.vehicle.PassengerSpace> {

    @Override
    public void mapAtoB(PassengerSpace netexPassengerSpace, org.rutebanken.sobek.model.vehicle.PassengerSpace sobekPassengerSpace, MappingContext context) {
        super.mapAtoB(netexPassengerSpace, sobekPassengerSpace, context);

        sobekPassengerSpace.setIncomingId(netexPassengerSpace.getId());
        if(netexPassengerSpace.getParentDeckSpaceRef() != null &&
                netexPassengerSpace.getParentDeckSpaceRef().getRef() != null) {
            var sobekParentDeckSpace = mapDeckSpaceNeTEx2Sobek(netexPassengerSpace.getParentDeckSpaceRef());
            if (sobekParentDeckSpace == null) {
                // We need to do this mapping in DeckMapper, when all the deckSpaces have gotten an Id value
                sobekPassengerSpace.setParentDeckSpaceRefNotMapped(netexPassengerSpace.getParentDeckSpaceRef().getRef());
            } else {
                sobekPassengerSpace.setParentDeckSpace(sobekParentDeckSpace);
            }
        }
        if (netexPassengerSpace.getDeckSpaceCapacities() != null &&
                netexPassengerSpace.getDeckSpaceCapacities().getDeckSpaceCapacity() != null &&
                !netexPassengerSpace.getDeckSpaceCapacities().getDeckSpaceCapacity().isEmpty()) {
            var rawDeckSpaceCapacities = netexPassengerSpace.getDeckSpaceCapacities().getDeckSpaceCapacity();
            List<org.rutebanken.sobek.model.vehicle.DeckSpaceCapacity> sobekDeckSpaceCapacities = mapperFacade.mapAsList(rawDeckSpaceCapacities, org.rutebanken.sobek.model.vehicle.DeckSpaceCapacity.class, context);
            if (!sobekDeckSpaceCapacities.isEmpty()) {
                sobekPassengerSpace.setDeckSpaceCapacities(sobekDeckSpaceCapacities);
            }
        }
        if (netexPassengerSpace.getDeckEntrances() != null &&
                netexPassengerSpace.getDeckEntrances().getDeckEntranceRefOrDeckEntrance_Dummy() != null &&
                !netexPassengerSpace.getDeckEntrances().getDeckEntranceRefOrDeckEntrance_Dummy().isEmpty()) {
            var rawDeckEntrances = netexPassengerSpace.getDeckEntrances().getDeckEntranceRefOrDeckEntrance_Dummy().stream().map(JAXBElement::getValue).toList();
            List<org.rutebanken.sobek.model.vehicle.PassengerEntrance> sobekPassengerEntrances = mapperFacade.mapAsList(rawDeckEntrances, org.rutebanken.sobek.model.vehicle.PassengerEntrance.class, context);
            if (!sobekPassengerEntrances.isEmpty()) {
                sobekPassengerSpace.setDeckEntrances(sobekPassengerEntrances);
            }
        }
        if (netexPassengerSpace.getPassengerSpots() != null &&
                netexPassengerSpace.getPassengerSpots().getPassengerSpotRefOrPassengerSpot() != null &&
                !netexPassengerSpace.getPassengerSpots().getPassengerSpotRefOrPassengerSpot().isEmpty()) {
            List<org.rutebanken.sobek.model.vehicle.PassengerSpot> sobekPassengerSpots = mapperFacade.mapAsList(netexPassengerSpace.getPassengerSpots().getPassengerSpotRefOrPassengerSpot(), org.rutebanken.sobek.model.vehicle.PassengerSpot.class, context);
            if (!sobekPassengerSpots.isEmpty()) {
                sobekPassengerSpace.setPassengerSpots(sobekPassengerSpots);
            }
        }
        if (netexPassengerSpace.getLuggageSpots() != null &&
                netexPassengerSpace.getLuggageSpots().getLuggageSpotRefOrLuggageSpot() != null &&
                !netexPassengerSpace.getLuggageSpots().getLuggageSpotRefOrLuggageSpot().isEmpty()) {
            List<org.rutebanken.sobek.model.vehicle.LuggageSpot> sobekLuggageSpots = mapperFacade.mapAsList(netexPassengerSpace.getLuggageSpots().getLuggageSpotRefOrLuggageSpot(), org.rutebanken.sobek.model.vehicle.LuggageSpot.class, context);
            if (!sobekLuggageSpots.isEmpty()) {
                sobekPassengerSpace.setLuggageSpots(sobekLuggageSpots);
            }
        }
        if(netexPassengerSpace.getSpotAffinities() != null &&
                netexPassengerSpace.getSpotAffinities().getSpotAffinity() != null &&
                !netexPassengerSpace.getSpotAffinities().getSpotAffinity().isEmpty()) {
            List<org.rutebanken.sobek.model.vehicle.SpotAffinity> sobekSpotAffinities = mapperFacade.mapAsList(netexPassengerSpace.getSpotAffinities().getSpotAffinity(), org.rutebanken.sobek.model.vehicle.SpotAffinity.class, context);
            if (!sobekSpotAffinities.isEmpty()) {
                sobekPassengerSpace.setSpotAffinities(sobekSpotAffinities);
            }
        }
        EquipmentMappingHelper equipmentMappingHelper = new EquipmentMappingHelper();
        equipmentMappingHelper.setFacade(this.mapperFacade);
        equipmentMappingHelper.mapActualEquipmentsNeTEx2Sobek(sobekPassengerSpace, netexPassengerSpace.getActualVehicleEquipments());
    }

    @Override
    public void mapBtoA(org.rutebanken.sobek.model.vehicle.PassengerSpace sobekPassengerSpace, PassengerSpace netexPassengerSpace, MappingContext context) {
        super.mapBtoA(sobekPassengerSpace, netexPassengerSpace, context);
        EquipmentMappingHelper equipmentMappingHelper = new EquipmentMappingHelper();
        equipmentMappingHelper.setFacade(this.mapperFacade);
        equipmentMappingHelper.mapActualEquipmentsSobek2NeTEx(netexPassengerSpace, sobekPassengerSpace.getActualVehicleEquipments());

        if(sobekPassengerSpace.getParentDeckSpace() != null) {
            netexPassengerSpace.setParentDeckSpaceRef(mapToNeTEx(sobekPassengerSpace.getParentDeckSpace()));
        }
    }

    private DeckSpace mapDeckSpaceNeTEx2Sobek(DeckSpaceRefStructure deckSpaceRefStructure) {
        if (deckSpaceRefStructure.getRef() == null) {
            return null;
        }

        return switch (deckSpaceRefStructure) {
            case org.rutebanken.netex.model.PassengerSpaceRefStructure ref ->
                    mapperFacade.map(ref, org.rutebanken.sobek.model.vehicle.PassengerSpace.class);
            default -> null;
        };
    }

    private DeckSpaceRefStructure mapToNeTEx(DeckSpace space) {
        return switch (space) {
            case org.rutebanken.sobek.model.vehicle.PassengerSpace passengerSpace -> new PassengerSpaceRefStructure().withRef(passengerSpace.getNetexId());
            default -> null;
        };
    }


}

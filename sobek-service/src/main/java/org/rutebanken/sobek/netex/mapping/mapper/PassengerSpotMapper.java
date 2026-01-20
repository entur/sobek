package org.rutebanken.sobek.netex.mapping.mapper;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.rutebanken.netex.model.PassengerSpot;
import org.rutebanken.netex.model.SpotColumnRefStructure;
import org.rutebanken.netex.model.SpotRowRefStructure;
import org.rutebanken.sobek.netex.mapping.EquipmentMappingHelper;

public class PassengerSpotMapper extends CustomMapper<PassengerSpot, org.rutebanken.sobek.model.vehicle.PassengerSpot> {

    @Override
    public void mapAtoB(PassengerSpot netexPassengerSpot, org.rutebanken.sobek.model.vehicle.PassengerSpot sobekPassengerSpot, MappingContext context) {
        super.mapAtoB(netexPassengerSpot, sobekPassengerSpot, context);

        EquipmentMappingHelper equipmentMappingHelper = new EquipmentMappingHelper();
        equipmentMappingHelper.setFacade(this.mapperFacade);
        equipmentMappingHelper.mapActualEquipmentsNeTEx2Sobek(sobekPassengerSpot, netexPassengerSpot.getActualVehicleEquipments());

        if(netexPassengerSpot.getSpotColumnRef() != null &&
                netexPassengerSpot.getSpotColumnRef().getRef() != null) {
            org.rutebanken.sobek.model.vehicle.Deck deck = (org.rutebanken.sobek.model.vehicle.Deck) context.getProperty("currentSobekDeck");
            if (deck != null && deck.getSpotColumns() != null) {
                String refId = netexPassengerSpot.getSpotColumnRef().getRef();
                deck.getSpotColumns().stream()
                        .filter(column -> refId.equals(column.getNetexId()))
                        .findFirst()
                        .ifPresent(sobekPassengerSpot::setSpotColumn);
            }
        }

        if(netexPassengerSpot.getSpotRowRef() != null &&
                netexPassengerSpot.getSpotRowRef().getRef() != null) {
            org.rutebanken.sobek.model.vehicle.Deck deck = (org.rutebanken.sobek.model.vehicle.Deck) context.getProperty("currentSobekDeck");
            if (deck != null && deck.getSpotRows() != null) {
                String refId = netexPassengerSpot.getSpotRowRef().getRef();
                deck.getSpotRows().stream()
                        .filter(column -> refId.equals(column.getNetexId()))
                        .findFirst()
                        .ifPresent(sobekPassengerSpot::setSpotRow);
            }
        }
    }

    @Override
    public void mapBtoA(org.rutebanken.sobek.model.vehicle.PassengerSpot sobekPassengerSpot, PassengerSpot netexPassengerSpot, MappingContext context) {
        super.mapBtoA(sobekPassengerSpot, netexPassengerSpot, context);

        EquipmentMappingHelper equipmentMappingHelper = new EquipmentMappingHelper();
        equipmentMappingHelper.setFacade(this.mapperFacade);
        equipmentMappingHelper.mapActualEquipmentsSobek2NeTEx(netexPassengerSpot, sobekPassengerSpot.getActualVehicleEquipments());

        if(sobekPassengerSpot.getSpotColumn() != null) {
            netexPassengerSpot.setSpotColumnRef(new SpotColumnRefStructure().withRef(sobekPassengerSpot.getSpotColumn().getNetexId()) );
        }

        if(sobekPassengerSpot.getSpotRow() != null) {
            netexPassengerSpot.setSpotRowRef(new SpotRowRefStructure().withRef(sobekPassengerSpot.getSpotRow().getNetexId()) );
        }
    }
}

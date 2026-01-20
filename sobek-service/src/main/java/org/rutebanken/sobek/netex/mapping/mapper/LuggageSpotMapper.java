package org.rutebanken.sobek.netex.mapping.mapper;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.rutebanken.netex.model.LuggageSpot;
import org.rutebanken.netex.model.SpotColumnRefStructure;
import org.rutebanken.netex.model.SpotRowRefStructure;
import org.rutebanken.sobek.netex.mapping.EquipmentMappingHelper;

public class LuggageSpotMapper extends CustomMapper<LuggageSpot, org.rutebanken.sobek.model.vehicle.LuggageSpot> {

    @Override
    public void mapAtoB(LuggageSpot netexLuggageSpot, org.rutebanken.sobek.model.vehicle.LuggageSpot sobekLuggageSpot, MappingContext context) {
        super.mapAtoB(netexLuggageSpot, sobekLuggageSpot, context);
        EquipmentMappingHelper equipmentMappingHelper = new EquipmentMappingHelper();
        equipmentMappingHelper.setFacade(this.mapperFacade);
        equipmentMappingHelper.mapActualEquipmentsNeTEx2Sobek(sobekLuggageSpot, netexLuggageSpot.getActualVehicleEquipments());
    }

    @Override
    public void mapBtoA(org.rutebanken.sobek.model.vehicle.LuggageSpot sobekLuggageSpot, LuggageSpot netexLuggageSpot, MappingContext context) {
        super.mapBtoA(sobekLuggageSpot, netexLuggageSpot, context);

        if (sobekLuggageSpot.getSpotRow() != null) {
            netexLuggageSpot.withSpotRowRef(new SpotRowRefStructure().withRef(sobekLuggageSpot.getSpotRow().getNetexId()));
        }
        if (sobekLuggageSpot.getSpotColumn() != null) {
            netexLuggageSpot.withSpotColumnRef(new SpotColumnRefStructure().withRef(sobekLuggageSpot.getSpotColumn().getNetexId()));
        }
        EquipmentMappingHelper equipmentMappingHelper = new EquipmentMappingHelper();
        equipmentMappingHelper.setFacade(this.mapperFacade);
        equipmentMappingHelper.mapActualEquipmentsSobek2NeTEx(netexLuggageSpot, sobekLuggageSpot.getActualVehicleEquipments());
    }
}

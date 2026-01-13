package org.rutebanken.sobek.netex.mapping.mapper;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MappingContext;
import org.rutebanken.netex.model.PassengerEntrance;
import org.rutebanken.sobek.netex.mapping.EquipmentMappingHelper;

public class PassengerEntranceMapper extends CustomMapper<PassengerEntrance, org.rutebanken.sobek.model.vehicle.PassengerEntrance> {

    @Override
    public void mapAtoB(PassengerEntrance netexPassengerEntrance, org.rutebanken.sobek.model.vehicle.PassengerEntrance sobekPassengerEntrance, MappingContext context) {
        super.mapAtoB(netexPassengerEntrance, sobekPassengerEntrance, context);

        EquipmentMappingHelper equipmentMappingHelper = new EquipmentMappingHelper();
        equipmentMappingHelper.setFacade(this.mapperFacade);
        equipmentMappingHelper.mapActualEquipmentsNeTEx2Sobek(sobekPassengerEntrance, netexPassengerEntrance.getActualVehicleEquipments());

    }

    @Override
    public void mapBtoA(org.rutebanken.sobek.model.vehicle.PassengerEntrance sobekPassengerEntrance, PassengerEntrance netexPassengerEntrance, MappingContext context) {
        super.mapBtoA(sobekPassengerEntrance, netexPassengerEntrance, context);
        EquipmentMappingHelper equipmentMappingHelper = new EquipmentMappingHelper();
        equipmentMappingHelper.setFacade(this.mapperFacade);
        equipmentMappingHelper.mapActualEquipmentsSobek2NeTEx(netexPassengerEntrance, sobekPassengerEntrance.getActualVehicleEquipments());
    }
}

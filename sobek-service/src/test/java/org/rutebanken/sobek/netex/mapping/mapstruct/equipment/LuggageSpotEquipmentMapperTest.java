package org.rutebanken.sobek.netex.mapping.mapstruct.equipment;

import org.rutebanken.netex.model.LuggageSpotEquipment;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapperTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LuggageSpotEquipmentMapperTest extends DataManagedObjectStructureMapperTestBase<
    LuggageSpotEquipment,
    org.rutebanken.sobek.model.vehicle.LuggageSpotEquipment> {

    @Autowired
    private LuggageSpotEquipmentMapper mapper;

    protected LuggageSpotEquipmentMapperTest() {
        super(LuggageSpotEquipment.class, org.rutebanken.sobek.model.vehicle.LuggageSpotEquipment.class);
    }

    @Override
    protected org.rutebanken.sobek.model.vehicle.LuggageSpotEquipment mapToSobek(LuggageSpotEquipment source) {
        return mapper.mapToSobek(source, context);
    }

    @Override
    protected LuggageSpotEquipment mapToNetex(org.rutebanken.sobek.model.vehicle.LuggageSpotEquipment source) {
        return mapper.mapToNetex(source, context);
    }
}

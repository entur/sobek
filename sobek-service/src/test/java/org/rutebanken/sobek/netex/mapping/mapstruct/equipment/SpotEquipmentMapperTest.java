package org.rutebanken.sobek.netex.mapping.mapstruct.equipment;

import org.rutebanken.netex.model.SpotEquipment;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapperTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SpotEquipmentMapperTest extends DataManagedObjectStructureMapperTestBase<
    SpotEquipment,
    org.rutebanken.sobek.model.vehicle.SpotEquipment> {

    @Autowired
    private SpotEquipmentMapper mapper;

    protected SpotEquipmentMapperTest() {
        super(SpotEquipment.class, org.rutebanken.sobek.model.vehicle.SpotEquipment.class);
    }

    @Override
    protected org.rutebanken.sobek.model.vehicle.SpotEquipment mapToSobek(SpotEquipment source) {
        return mapper.mapToSobek(source, context);
    }

    @Override
    protected SpotEquipment mapToNetex(org.rutebanken.sobek.model.vehicle.SpotEquipment source) {
        return mapper.mapToNetex(source, context);
    }
}

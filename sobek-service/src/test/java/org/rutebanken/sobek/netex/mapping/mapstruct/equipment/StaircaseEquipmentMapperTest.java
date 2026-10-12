package org.rutebanken.sobek.netex.mapping.mapstruct.equipment;

import org.rutebanken.netex.model.StaircaseEquipment;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapperTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class StaircaseEquipmentMapperTest extends DataManagedObjectStructureMapperTestBase<
    StaircaseEquipment,
    org.rutebanken.sobek.model.vehicle.StaircaseEquipment> {

    @Autowired
    private StaircaseEquipmentMapper mapper;

    protected StaircaseEquipmentMapperTest() {
        super(StaircaseEquipment.class, org.rutebanken.sobek.model.vehicle.StaircaseEquipment.class);
    }

    @Override
    protected org.rutebanken.sobek.model.vehicle.StaircaseEquipment mapToSobek(StaircaseEquipment source) {
        return mapper.mapToSobek(source, context);
    }

    @Override
    protected StaircaseEquipment mapToNetex(org.rutebanken.sobek.model.vehicle.StaircaseEquipment source) {
        return mapper.mapToNetex(source, context);
    }
}

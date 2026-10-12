package org.rutebanken.sobek.netex.mapping.mapstruct.equipment;

import org.rutebanken.netex.model.BedEquipment;
import org.rutebanken.sobek.netex.mapping.mapstruct.DataManagedObjectStructureMapperTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BedEquipmentMapperTest extends DataManagedObjectStructureMapperTestBase<
    BedEquipment,
    org.rutebanken.sobek.model.vehicle.BedEquipment> {

    @Autowired
    private BedEquipmentMapper mapper;

    protected BedEquipmentMapperTest() {
        super(org.rutebanken.netex.model.BedEquipment.class, org.rutebanken.sobek.model.vehicle.BedEquipment.class);
    }

    @Override
    protected org.rutebanken.sobek.model.vehicle.BedEquipment mapToSobek(org.rutebanken.netex.model.BedEquipment source) {
        return mapper.mapToSobek(source, context);
    }

    @Override
    protected org.rutebanken.netex.model.BedEquipment mapToNetex(org.rutebanken.sobek.model.vehicle.BedEquipment source) {
        return mapper.mapToNetex(source, context);
    }
}
